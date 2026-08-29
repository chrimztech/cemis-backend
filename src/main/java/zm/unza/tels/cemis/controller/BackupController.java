package zm.unza.tels.cemis.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zm.unza.tels.cemis.service.BackupService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final BackupService backupService;

    /** Storage inventory (file counts/sizes) for certificates + branding buckets. */
    @GetMapping("/manifest")
    public ResponseEntity<Map<String, Object>> manifest() throws IOException {
        return ResponseEntity.ok(backupService.buildStorageManifest());
    }

    /** Downloads one ZIP with a full Postgres dump plus every certificate PDF and branding asset. */
    @GetMapping("/full")
    public void downloadFull(HttpServletResponse response) throws IOException {
        String filename = "cemis-backup-" + LocalDate.now() + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        backupService.writeFullBackupZip(response.getOutputStream());
    }
}
