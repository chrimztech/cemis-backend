package zm.unza.tels.cemis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT c FROM Certificate c LEFT JOIN FETCH c.student LEFT JOIN FETCH c.course " +
           "WHERE (:q = '' " +
           "  OR LOWER(c.recipientName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.recipientEmail) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.nationalId) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.certificateCode) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.programme) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:status IS NULL OR CAST(c.status AS string) = :status) " +
           "AND (:emailStatus IS NULL OR c.emailStatus = :emailStatus)",
        countQuery = "SELECT COUNT(c) FROM Certificate c " +
           "WHERE (:q = '' " +
           "  OR LOWER(c.recipientName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.recipientEmail) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.nationalId) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.certificateCode) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.programme) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:status IS NULL OR CAST(c.status AS string) = :status) " +
           "AND (:emailStatus IS NULL OR c.emailStatus = :emailStatus)")
    Page<Certificate> search(
        @Param("q") String q,
        @Param("status") String status,
        @Param("emailStatus") String emailStatus,
        Pageable pageable
    );
}
