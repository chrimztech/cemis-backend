package zm.unza.tels.cemis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zm.unza.tels.cemis.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
