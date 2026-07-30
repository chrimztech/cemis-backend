package zm.unza.tels.cemis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String prefix;

    @Column(nullable = false)
    private String name;

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private String category = "general";

    private String mode;

    @Column(name = "duration_text")
    private String durationText;

    @Column(name = "fee_unza", precision = 10, scale = 2)
    private BigDecimal feeUnza;

    @Column(name = "fee_non_unza", precision = 10, scale = 2)
    private BigDecimal feeNonUnza;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "time_slot")
    private String timeSlot;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", columnDefinition = "certificate_type")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private CertificateType certificateType = CertificateType.competence;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
