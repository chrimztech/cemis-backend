package zm.unza.tels.cemis.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class BrandingController {

    @Value("${app.branding.dir:./branding-assets}")
    private String brandingDir;

    private Path brandingPath() throws IOException {
        Path p = Paths.get(brandingDir);
        Files.createDirectories(p);
        return p;
    }

    /** List files in the branding directory. */
    @GetMapping("/branding")
    public ResponseEntity<List<Map<String, Object>>> list() throws IOException {
        File[] files = brandingPath().toFile().listFiles(File::isFile);
        List<Map<String, Object>> result = files == null ? List.of()
            : Arrays.stream(files)
                .map(f -> Map.<String, Object>of("name", f.getName(), "size", f.length()))
                .toList();
        return ResponseEntity.ok(result);
    }

    /** Download a branding asset (requires auth). */
    @GetMapping("/branding/{filename}")
    public ResponseEntity<FileSystemResource> download(@PathVariable String filename) throws IOException {
        Path file = brandingPath().resolve(filename);
        if (!file.toFile().exists()) return ResponseEntity.notFound().build();
        String ct = Files.probeContentType(file);
        return ResponseEntity.ok()
            .contentType(ct != null ? MediaType.parseMediaType(ct) : MediaType.APPLICATION_OCTET_STREAM)
            .body(new FileSystemResource(file));
    }

    /** Public download — no auth required (used for img src / signedUrl replacement). */
    @GetMapping("/branding/public/{filename}")
    public ResponseEntity<FileSystemResource> downloadPublic(@PathVariable String filename) throws IOException {
        return download(filename);
    }

    /** Upload / replace a branding asset. */
    @PostMapping("/branding/{filename}")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable String filename,
            @RequestParam("file") MultipartFile file) throws IOException {
        Path dest = brandingPath().toAbsolutePath().resolve(filename);
        try (var in = file.getInputStream()) {
            java.nio.file.Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return ResponseEntity.ok(Map.of("ok", true, "path", filename));
    }

    /** Delete a branding asset. */
    @DeleteMapping("/branding/{filename}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String filename) throws IOException {
        Path file = brandingPath().resolve(filename);
        Files.deleteIfExists(file);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
