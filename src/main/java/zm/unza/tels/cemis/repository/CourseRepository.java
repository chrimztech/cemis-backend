package zm.unza.tels.cemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.unza.tels.cemis.entity.Course;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findAllByOrderByNameAsc();
    List<Course> findByActiveTrueOrderByNameAsc();
    boolean existsByCode(String code);
}
