package zm.unza.tels.cemis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "org_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrgSettings {

    @Id
    private Boolean id = true;

    @Column(name = "org_name",        nullable = false) private String orgName       = "CICT-TeLS";
    @Column(name = "org_prefix",      nullable = false) private String orgPrefix     = "TELS";
    @Column(name = "signatory1_name", nullable = false) private String signatory1Name  = "";
    @Column(name = "signatory1_title",nullable = false) private String signatory1Title = "";
    @Column(name = "signatory2_name", nullable = false) private String signatory2Name  = "";
    @Column(name = "signatory2_title",nullable = false) private String signatory2Title = "";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_layout")
    private Map<String, Object> templateLayout;

    @Column(name = "created_at", updatable = false) private Instant createdAt = Instant.now();
    @UpdateTimestamp
    @Column(name = "updated_at") private Instant updatedAt;
}
