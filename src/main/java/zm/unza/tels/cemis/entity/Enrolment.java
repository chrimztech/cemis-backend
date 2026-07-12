package zm.unza.tels.cemis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrolments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Enrolment {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enrolment_status")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private EnrolStatus status = EnrolStatus.enrolled;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", columnDefinition = "payment_status_type")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private PayStatus paymentStatus = PayStatus.pending;

    @Column(name = "fee_charged", precision = 10, scale = 2)
    private BigDecimal feeCharged;

    @Builder.Default
    @Column(name = "enrolled_at", updatable = false)
    private Instant enrolledAt = Instant.now();

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id")
    private Certificate certificate;

    private String notes;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum EnrolStatus { enrolled, in_progress, completed, certified }
    public enum PayStatus   { pending, paid, waived, free }
}
