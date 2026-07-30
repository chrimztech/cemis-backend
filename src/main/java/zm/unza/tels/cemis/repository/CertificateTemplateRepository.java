package zm.unza.tels.cemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.unza.tels.cemis.entity.CertificateTemplate;
import zm.unza.tels.cemis.entity.CertificateType;

public interface CertificateTemplateRepository extends JpaRepository<CertificateTemplate, CertificateType> {
}
