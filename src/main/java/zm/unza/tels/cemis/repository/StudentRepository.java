package zm.unza.tels.cemis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.unza.tels.cemis.entity.Student;

import java.util.List;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    @Query("SELECT s FROM Student s WHERE " +
           "(:q = '' " +
           "  OR LOWER(s.fullName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.email) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.nationalId) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(s.unzaStudentId) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:category IS NULL OR s.category = :category)")
    Page<Student> search(@Param("q") String query, @Param("category") String category, Pageable pageable);

    List<Student> findAllByOrderByFullNameAsc();

    java.util.Optional<Student> findByNationalId(String nationalId);
}
