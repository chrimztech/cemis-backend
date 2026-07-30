package zm.unza.tels.cemis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "certificate_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateTemplate {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", columnDefinition = "certificate_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private CertificateType certificateType;

    @Builder.Default
    @Column(name = "signatory1_name", nullable = false) private String signatory1Name  = "";
    @Builder.Default
    @Column(name = "signatory1_title", nullable = false) private String signatory1Title = "";
    @Builder.Default
    @Column(name = "signatory2_name", nullable = false) private String signatory2Name  = "";
    @Builder.Default
    @Column(name = "signatory2_title", nullable = false) private String signatory2Title = "";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_layout")
    private Map<String, Object> templateLayout;

    @Builder.Default
    @Column(name = "created_at", updatable = false) private Instant createdAt = Instant.now();
    @UpdateTimestamp
    @Column(name = "updated_at") private Instant updatedAt;
}
