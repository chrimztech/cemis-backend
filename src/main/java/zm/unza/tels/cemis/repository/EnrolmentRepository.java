package zm.unza.tels.cemis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.unza.tels.cemis.entity.Enrolment;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrolmentRepository extends JpaRepository<Enrolment, UUID> {

    @Query("SELECT e FROM Enrolment e JOIN FETCH e.student JOIN FETCH e.course LEFT JOIN FETCH e.certificate ORDER BY e.enrolledAt DESC")
    List<Enrolment> findAllWithDetails();

    @Query("SELECT e FROM Enrolment e JOIN FETCH e.student JOIN FETCH e.course LEFT JOIN FETCH e.certificate " +
           "WHERE e.status = :status ORDER BY e.enrolledAt DESC")
    List<Enrolment> findByStatusWithDetails(@Param("status") Enrolment.EnrolStatus status);

    @Query("SELECT e FROM Enrolment e JOIN FETCH e.student JOIN FETCH e.course LEFT JOIN FETCH e.certificate " +
           "WHERE e.id = :id")
    Optional<Enrolment> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT e FROM Enrolment e JOIN FETCH e.student JOIN FETCH e.course LEFT JOIN FETCH e.certificate " +
           "WHERE e.student.id = :studentId ORDER BY e.enrolledAt DESC")
    List<Enrolment> findByStudentIdWithDetails(@Param("studentId") UUID studentId);

    @Query("SELECT e FROM Enrolment e LEFT JOIN FETCH e.student LEFT JOIN FETCH e.course " +
           "WHERE e.status IN :statuses AND e.certificate IS NULL ORDER BY e.createdAt DESC")
    List<Enrolment> findByStatusInAndNoCertificate(@Param("statuses") List<Enrolment.EnrolStatus> statuses);

    @Query(
        value = "SELECT e FROM Enrolment e LEFT JOIN FETCH e.student LEFT JOIN FETCH e.course " +
            "WHERE e.status IN :statuses AND e.certificate IS NULL",
        countQuery = "SELECT COUNT(e) FROM Enrolment e " +
            "WHERE e.status IN :statuses AND e.certificate IS NULL"
    )
    Page<Enrolment> findByStatusInAndNoCertificate(
        @Param("statuses") List<Enrolment.EnrolStatus> statuses, Pageable pageable
    );

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    @Query("SELECT COUNT(e) FROM Enrolment e WHERE e.status = :status")
    long countByStatus(@Param("status") Enrolment.EnrolStatus status);

    @Query(value = "SELECT e FROM Enrolment e " +
           "JOIN FETCH e.student s JOIN FETCH e.course c LEFT JOIN FETCH e.certificate cert " +
           "WHERE (:q = '' " +
           "  OR LOWER(s.fullName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.email) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.nationalId) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.unzaStudentId) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.prefix) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:courseId IS NULL OR c.id = :courseId) " +
           "AND (:status IS NULL OR CAST(e.status AS string) = :status) " +
           "AND e.enrolledAt >= :fromDate " +
           "AND e.enrolledAt <= :toDate",
        countQuery = "SELECT COUNT(e) FROM Enrolment e " +
           "JOIN e.student s JOIN e.course c " +
           "WHERE (:q = '' " +
           "  OR LOWER(s.fullName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.email) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.nationalId) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.unzaStudentId) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(c.prefix) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:courseId IS NULL OR c.id = :courseId) " +
           "AND (:status IS NULL OR CAST(e.status AS string) = :status) " +
           "AND e.enrolledAt >= :fromDate " +
           "AND e.enrolledAt <= :toDate")
    Page<Enrolment> search(
        @Param("q") String q,
        @Param("courseId") UUID courseId,
        @Param("status") String status,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        Pageable pageable
    );
}
