package zm.unza.tels.cemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.unza.tels.cemis.entity.OrgSettings;

public interface OrgSettingsRepository extends JpaRepository<OrgSettings, Boolean> {
}
