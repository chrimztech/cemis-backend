package zm.unza.tels.cemis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zm.unza.tels.cemis.entity.Course;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findAllByOrderByNameAsc();
    List<Course> findByActiveTrueOrderByNameAsc();
    boolean existsByCode(String code);

    @Query("SELECT c FROM Course c WHERE " +
           "(:q = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(c.code) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(c.prefix) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:activeOnly = false OR c.active = true) " +
           "AND (:category IS NULL OR c.category = :category)")
    Page<Course> search(
        @Param("q") String query,
        @Param("activeOnly") boolean activeOnly,
        @Param("category") String category,
        Pageable pageable
    );
}
