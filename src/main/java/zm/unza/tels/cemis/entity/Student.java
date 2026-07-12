package zm.unza.tels.cemis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String email;
    private String phone;

    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "unza_student_id")
    private String unzaStudentId;

    @Builder.Default
    @Column(nullable = false)
    private String category = "non_unza";

    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @Column(name = "pii_consent_at")
    private Instant piiConsentAt;

    @Column(name = "pii_consent_source")
    private String piiConsentSource;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
