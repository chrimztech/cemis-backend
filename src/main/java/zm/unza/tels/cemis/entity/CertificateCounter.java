package zm.unza.tels.cemis.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "certificate_counters")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateCounter {

    @Id
    private String prefix;

    @Column(name = "last_value", nullable = false)
    private int lastValue = 0;
}
