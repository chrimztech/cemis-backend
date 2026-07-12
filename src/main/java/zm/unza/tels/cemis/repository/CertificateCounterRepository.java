package zm.unza.tels.cemis.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.unza.tels.cemis.entity.CertificateCounter;

import java.util.Optional;

public interface CertificateCounterRepository extends JpaRepository<CertificateCounter, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CertificateCounter c WHERE c.prefix = :prefix")
    Optional<CertificateCounter> findByPrefixForUpdate(@Param("prefix") String prefix);
}
