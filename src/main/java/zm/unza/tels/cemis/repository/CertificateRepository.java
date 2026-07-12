package zm.unza.tels.cemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zm.unza.tels.cemis.entity.Certificate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByCertificateCode(String code);
    Optional<Certificate> findByCertificateId(String id);
    Optional<Certificate> findByStudent_IdAndCourse_Id(UUID studentId, UUID courseId);

    @Query("SELECT c FROM Certificate c LEFT JOIN FETCH c.student LEFT JOIN FETCH c.course ORDER BY c.createdAt DESC")
    List<Certificate> findAllWithDetails();

    long countByStatus(Certificate.CertStatus status);
    long countByEmailStatus(String emailStatus);
}
