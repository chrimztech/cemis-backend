package zm.unza.tels.cemis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "org_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrgSettings {

    @Id
    private Boolean id = true;

    @Column(name = "org_name",        nullable = false) private String orgName       = "CICT-TeLS";
    @Column(name = "org_prefix",      nullable = false) private String orgPrefix     = "TELS";

    @Column(name = "created_at", updatable = false) private Instant createdAt = Instant.now();
    @UpdateTimestamp
    @Column(name = "updated_at") private Instant updatedAt;
}
