package zm.unza.tels.cemis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zm.unza.tels.cemis.entity.StudentAccessLog;

import java.util.UUID;

public interface StudentAccessLogRepository extends JpaRepository<StudentAccessLog, UUID> {

    @Query("SELECT l FROM StudentAccessLog l LEFT JOIN FETCH l.student LEFT JOIN FETCH l.actor ORDER BY l.createdAt DESC")
    Page<StudentAccessLog> findAllWithDetails(Pageable pageable);
}
