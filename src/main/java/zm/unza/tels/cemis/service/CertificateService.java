package zm.unza.tels.cemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.tels.cemis.entity.Certificate;
import zm.unza.tels.cemis.entity.CertificateType;
import zm.unza.tels.cemis.entity.Enrolment;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.exception.ResourceNotFoundException;
import zm.unza.tels.cemis.repository.CertificateRepository;
import zm.unza.tels.cemis.repository.EnrolmentRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certRepository;
    private final EnrolmentRepository   enrolmentRepository;

    @Value("${app.cert.signing-secret}")
    private String signingSecret;

    @Transactional(readOnly = true)
    public List<Certificate> listAll() {
        return certRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Certificate getById(UUID id) {
        return certRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    }

    @Transactional(readOnly = true)
    public Optional<Certificate> findByCode(String code) {
        var found = certRepository.findByCertificateCode(code);
        return found.isPresent() ? found : certRepository.findByCertificateId(code);
    }

    @Transactional
    public Certificate generate(UUID enrolmentId, User issuedBy, String issuerName) {
        var enrolment = enrolmentRepository.findById(enrolmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrolment not found"));

        if (enrolment.getCertificate() != null)
            return enrolment.getCertificate();

        var student = enrolment.getStudent();
        var course  = enrolment.getCourse();

        // One certificate per student+course — return existing if already issued for same course
        if (student != null && course != null) {
            var existing = certRepository.findByStudent_IdAndCourse_Id(student.getId(), course.getId());
            if (existing.isPresent()) {
                var cert = existing.get();
                enrolment.setCertificate(cert);
                enrolment.setStatus(Enrolment.EnrolStatus.certified);
                enrolmentRepository.save(enrolment);
                return cert;
            }
        }

        String prefix = course != null ? course.getPrefix() : "TELS";
        String code   = nextCode(prefix);
        String certId = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        var cert = Certificate.builder()
            .certificateId(certId)
            .certificateCode(code)
            .student(student)
            .course(course)
            .recipientName(student != null ? student.getFullName() : "")
            .recipientEmail(student != null ? student.getEmail() : null)
            .programme(course != null ? course.getName() : "")
            .issuerName(issuerName != null ? issuerName : "CICT-TeLS")
            .issuedBy(issuedBy)
            .issueDate(LocalDate.now())
            .nationalId(student != null ? student.getNationalId() : null)
            .certificateType(course != null ? course.getCertificateType() : CertificateType.competence)
            .build();

        // Sign immediately
        signCert(cert);
        var saved = certRepository.save(cert);

        // Link to enrolment
        enrolment.setCertificate(saved);
        enrolment.setStatus(Enrolment.EnrolStatus.certified);
        if (enrolment.getCompletedAt() == null) enrolment.setCompletedAt(java.time.Instant.now());
        enrolmentRepository.save(enrolment);

        return saved;
    }

    @Transactional
    public Certificate partialUpdate(UUID id, Map<String, Object> fields) {
        var cert = getById(id);
        if (fields.containsKey("status")) {
            cert.setStatus(Certificate.CertStatus.valueOf(fields.get("status").toString()));
        }
        if (fields.containsKey("revoked_at")) {
            Object val = fields.get("revoked_at");
            cert.setRevokedAt(val != null ? java.time.Instant.parse(val.toString()) : null);
        }
        if (fields.containsKey("revoke_reason")) {
            Object val = fields.get("revoke_reason");
            cert.setRevokeReason(val != null ? val.toString() : null);
        }
        if (fields.containsKey("email_status")) {
            cert.setEmailStatus(fields.get("email_status").toString());
        }

        // Editable embedded fields — fixes typos/data-entry mistakes (e.g. NRC entered
        // as the UNZA computer number) without needing to delete and regenerate.
        boolean resign = false;
        if (fields.containsKey("recipient_name")) {
            Object val = fields.get("recipient_name");
            cert.setRecipientName(val != null ? val.toString() : cert.getRecipientName());
        }
        if (fields.containsKey("recipient_email")) {
            Object val = fields.get("recipient_email");
            cert.setRecipientEmail(val != null ? val.toString() : null);
        }
        if (fields.containsKey("programme")) {
            Object val = fields.get("programme");
            cert.setProgramme(val != null ? val.toString() : cert.getProgramme());
        }
        if (fields.containsKey("national_id")) {
            Object val = fields.get("national_id");
            cert.setNationalId(val != null ? val.toString() : null);
        }
        if (fields.containsKey("issue_date")) {
            Object val = fields.get("issue_date");
            if (val != null) {
                cert.setIssueDate(java.time.LocalDate.parse(val.toString()));
                resign = true; // issue_date is part of the signed payload
            }
        }
        if (resign) {
            signCert(cert);
        }

        return certRepository.save(cert);
    }

    @Transactional
    public void delete(UUID id) {
        var cert = getById(id);
        // Unlink from enrolments before deleting
        enrolmentRepository.findAll().stream()
            .filter(e -> e.getCertificate() != null && e.getCertificate().getId().equals(id))
            .forEach(e -> { e.setCertificate(null); enrolmentRepository.save(e); });
        certRepository.delete(cert);
    }

    @Transactional
    public Certificate savePdfPath(UUID id, String path) {
        var cert = getById(id);
        cert.setPdfPath(path);
        return certRepository.save(cert);
    }

    @Transactional
    public Certificate markEmailSent(UUID id) {
        var cert = getById(id);
        cert.setEmailStatus("sent");
        cert.setEmailSentAt(java.time.Instant.now());
        cert.setEmailAttempts(cert.getEmailAttempts() + 1);
        return certRepository.save(cert);
    }

    @Transactional
    public Certificate markEmailFailed(UUID id, String error) {
        var cert = getById(id);
        cert.setEmailStatus("failed");
        cert.setEmailLastError(error);
        cert.setEmailAttempts(cert.getEmailAttempts() + 1);
        return certRepository.save(cert);
    }

    public record VerifyResult(boolean verified, String reason, Certificate certificate) {}

    public VerifyResult verify(String code) {
        var opt = findByCode(code);
        if (opt.isEmpty()) return new VerifyResult(false, "not_found", null);

        var cert = opt.get();
        if (cert.getSignature() == null || cert.getSignedPayload() == null)
            return new VerifyResult(false, "unsigned", cert);

        boolean ok = verifySignature(cert.getSignedPayload(), cert.getSignature());
        return new VerifyResult(ok, ok ? "ok" : "invalid_signature", cert);
    }

    // ── Certificate code generation ───────────────────────────────────────────

    private String nextCode(String prefix) {
        var now    = java.time.LocalDate.now();
        String month  = String.format("%02d", now.getMonthValue());
        String random = String.format("%06d",
            java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 1_000_000));
        return prefix + now.getYear() + month + random;
    }

    // ── HMAC-SHA256 signing ───────────────────────────────────────────────────

    private void signCert(Certificate cert) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("certificate_code", cert.getCertificateCode());
        payload.put("issued_at",        cert.getIssueDate().toString());
        payload.put("issuer_id",        cert.getIssuedBy() != null ? cert.getIssuedBy().getId().toString() : null);

        cert.setSignedPayload(payload);
        cert.setSignature(hmac(canonical(payload)));
    }

    private boolean verifySignature(Map<String, Object> payload, String signature) {
        try {
            return hmac(canonical(payload)).equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    // Postgres JSONB does not preserve object key insertion order on round-trip,
    // so Map.toString() gives a different string (and thus a different HMAC) after
    // the entity is reloaded from the DB than it did at signing time. Sorting keys
    // first makes the hashed representation independent of storage/reload order.
    private String canonical(Map<String, Object> payload) {
        return new java.util.TreeMap<>(payload).toString();
    }

    private String hmac(String data) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder();
            for (byte b : bytes) sb.append("%02x".formatted(b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }
}
