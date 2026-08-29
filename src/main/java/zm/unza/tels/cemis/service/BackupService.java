package zm.unza.tels.cemis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class BackupService {

    @Value("${app.cert.pdf-dir:./cert-pdfs}")
    private String pdfDir;

    @Value("${app.branding.dir:./branding-assets}")
    private String brandingDir;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${app.backup.pg-dump-path:pg_dump}")
    private String pgDumpPath;

    /** Storage inventory across both file buckets, for the admin Backup & export screen. */
    public Map<String, Object> buildStorageManifest() throws IOException {
        return Map.of(
            "certificates", bucketSummary(listFiles(Paths.get(pdfDir))),
            "branding", bucketSummary(listFiles(Paths.get(brandingDir)))
        );
    }

    private List<Map<String, Object>> listFiles(Path dir) throws IOException {
        Files.createDirectories(dir);
        File[] files = dir.toFile().listFiles(File::isFile);
        if (files == null) return List.of();
        return Arrays.stream(files)
            .map(f -> Map.<String, Object>of(
                "name", f.getName(),
                "size", f.length(),
                "updated_at", Instant.ofEpochMilli(f.lastModified()).toString()
            ))
            .toList();
    }

    private Map<String, Object> bucketSummary(List<Map<String, Object>> files) {
        long total = files.stream().mapToLong(f -> (Long) f.get("size")).sum();
        return Map.of("count", files.size(), "total_bytes", total, "files", files);
    }

    /**
     * Streams a single ZIP containing a full Postgres dump plus every certificate PDF and
     * branding asset on disk — "everything" for disaster recovery, in one download.
     * A failed pg_dump (e.g. binary not installed) does not abort the whole archive; the
     * failure is recorded inside database.sql so the file backups still come through.
     */
    public void writeFullBackupZip(OutputStream responseOut) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(responseOut)) {
            writeDatabaseDump(zip);
            writeDirectoryEntries(zip, Paths.get(pdfDir), "certificates/");
            writeDirectoryEntries(zip, Paths.get(brandingDir), "branding/");
        }
    }

    private void writeDatabaseDump(ZipOutputStream zip) throws IOException {
        zip.putNextEntry(new ZipEntry("database.sql"));
        try {
            runPgDump(zip);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            String msg = "-- pg_dump failed, database is NOT included in this archive: "
                + e.getMessage() + "\n";
            zip.write(msg.getBytes(StandardCharsets.UTF_8));
        }
        zip.closeEntry();
    }

    private void runPgDump(ZipOutputStream zip) throws IOException, InterruptedException {
        DbConn conn = parseJdbcUrl(dbUrl);
        List<String> cmd = new ArrayList<>(List.of(
            pgDumpPath, "-h", conn.host(), "-p", conn.port(), "-U", dbUsername,
            "-d", conn.database(), "--no-password", "--format=plain",
            "--no-owner", "--no-privileges"
        ));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().put("PGPASSWORD", dbPassword);
        Process process = pb.start();

        // Drain stderr on its own thread so a full pipe buffer can't deadlock pg_dump
        // while we're blocked reading stdout.
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
        Thread stderrThread = new Thread(() -> {
            try {
                process.getErrorStream().transferTo(stderrBuffer);
            } catch (IOException ignored) {
                // best-effort — only used for the error message on non-zero exit
            }
        });
        stderrThread.start();

        process.getInputStream().transferTo(zip);
        stderrThread.join();
        int exit = process.waitFor();
        if (exit != 0) {
            throw new IOException("pg_dump exited " + exit + ": "
                + stderrBuffer.toString(StandardCharsets.UTF_8).trim());
        }
    }

    private record DbConn(String host, String port, String database) {}

    private DbConn parseJdbcUrl(String url) {
        Matcher m = Pattern.compile("jdbc:postgresql://([^:/]+)(?::(\\d+))?/([^?]+)").matcher(url);
        if (!m.find()) throw new IllegalStateException("Cannot parse datasource URL: " + url);
        return new DbConn(m.group(1), m.group(2) != null ? m.group(2) : "5432", m.group(3));
    }

    private void writeDirectoryEntries(ZipOutputStream zip, Path dir, String entryPrefix) throws IOException {
        Files.createDirectories(dir);
        File[] files = dir.toFile().listFiles(File::isFile);
        if (files == null) return;
        for (File f : files) {
            zip.putNextEntry(new ZipEntry(entryPrefix + f.getName()));
            try (FileInputStream in = new FileInputStream(f)) {
                in.transferTo(zip);
            }
            zip.closeEntry();
        }
    }
}
