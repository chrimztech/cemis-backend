package zm.unza.tels.cemis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "certificate_id", unique = true, nullable = false)
    private String certificateId;

    @Column(name = "certificate_code", unique = true)
    private String certificateCode;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(nullable = false)
    private String programme;

    @Builder.Default
    @Column(name = "issuer_name", nullable = false)
    private String issuerName = "CICT-TeLS";

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by")
    private User issuedBy;

    @Builder.Default
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "certificate_status")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CertStatus status = CertStatus.valid;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason")
    private String revokeReason;

    @Builder.Default
    @Column(name = "email_status", nullable = false)
    private String emailStatus = "pending";

    @Column(name = "email_sent_at")
    private Instant emailSentAt;

    @Builder.Default
    @Column(name = "email_attempts")
    private int emailAttempts = 0;

    @Column(name = "email_last_error")
    private String emailLastError;

    @Column(name = "pdf_path")
    private String pdfPath;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "signed_payload")
    private Map<String, Object> signedPayload;

    private String signature;

    @Column(name = "national_id")
    private String nationalId;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum CertStatus { valid, revoked }

    @Transient
    @JsonProperty("student_id")
    public UUID getStudentId() {
        return student != null ? student.getId() : null;
    }

    @Transient
    @JsonProperty("course_id")
    public UUID getCourseId() {
        return course != null ? course.getId() : null;
    }

    @Transient
    @JsonProperty("issued_by_id")
    public UUID getIssuedById() {
        return issuedBy != null ? issuedBy.getId() : null;
    }
}
