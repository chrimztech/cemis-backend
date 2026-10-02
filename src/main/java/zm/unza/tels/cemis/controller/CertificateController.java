package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zm.unza.tels.cemis.entity.Certificate;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.repository.OrgSettingsRepository;
import zm.unza.tels.cemis.repository.UserRepository;
import zm.unza.tels.cemis.security.CemisUserDetails;
import zm.unza.tels.cemis.service.CertificateService;
import zm.unza.tels.cemis.service.EmailService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService    certService;
    private final EmailService          emailService;
    private final UserRepository        userRepository;
    private final OrgSettingsRepository settingsRepository;

    @Value("${app.cert.pdf-dir:./cert-pdfs}")
    private String pdfDir;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String emailStatus,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null) {
            Certificate.CertStatus statusEnum = status != null ? Certificate.CertStatus.valueOf(status) : null;
            return ResponseEntity.ok(certService.search(q, statusEnum, emailStatus, page, size == null ? 25 : size));
        }
        return ResponseEntity.ok(certService.listAll());
    }

    @GetMapping("/{id}")
    public Certificate get(@PathVariable UUID id) {
        return certService.getById(id);
    }

    @PostMapping("/generate")
    public ResponseEntity<Certificate> generate(@RequestBody Map<String, String> body,
                                                @AuthenticationPrincipal CemisUserDetails principal) {
        UUID enrolmentId = UUID.fromString(body.get("enrolmentId"));
        User actor       = userRepository.findById(principal.getId()).orElse(null);
        String orgName   = settingsRepository.findById(true)
            .map(s -> s.getOrgName()).orElse("CICT-TeLS");
        return ResponseEntity.ok(certService.generate(enrolmentId, actor, orgName));
    }

    /** Upload a PDF from the client and store it on the server's filesystem */
    @PostMapping("/{id}/pdf")
    public ResponseEntity<Map<String, Object>> uploadPdf(@PathVariable UUID id,
                                                         @RequestParam("file") MultipartFile file) throws IOException {
        var cert    = certService.getById(id);
        String code = cert.getCertificateCode() != null ? cert.getCertificateCode() : cert.getCertificateId();
        Path  dir   = Paths.get(pdfDir).toAbsolutePath();
        Files.createDirectories(dir);
        Path  dest  = dir.resolve(code + ".pdf");
        try (var in = file.getInputStream()) {
            Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        certService.savePdfPath(id, dest.toAbsolutePath().toString());
        return ResponseEntity.ok(Map.of("ok", true, "path", dest.toAbsolutePath().toString()));
    }

    /** Download a PDF directly from the server */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<FileSystemResource> downloadPdf(@PathVariable UUID id) {
        var cert   = certService.getById(id);
        String code = cert.getCertificateCode() != null ? cert.getCertificateCode() : cert.getCertificateId();
        File   file = cert.getPdfPath() != null ? new File(cert.getPdfPath())
                    : new File(pdfDir, code + ".pdf");
        if (!file.exists())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + code + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(new FileSystemResource(file));
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<Map<String, Object>> sendEmail(@PathVariable UUID id) {
        var cert = certService.getById(id);
        try {
            emailService.sendCertificateEmail(cert);
            certService.markEmailSent(id);
            return ResponseEntity.ok(Map.of("ok", true, "sentTo", cert.getRecipientEmail()));
        } catch (Exception e) {
            certService.markEmailFailed(id, e.getMessage());
            throw new RuntimeException("Email delivery failed: " + e.getMessage(), e);
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Certificate> partialUpdate(@PathVariable UUID id,
                                                     @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(certService.partialUpdate(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        certService.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/verify/{code}")
    public ResponseEntity<Map<String, Object>> verify(@PathVariable String code) {
        var result = certService.verify(code);
        return ResponseEntity.ok(Map.of(
            "verified",    result.verified(),
            "reason",      result.reason(),
            "certificate", result.certificate() != null ? result.certificate() : Map.of()
        ));
    }
}
