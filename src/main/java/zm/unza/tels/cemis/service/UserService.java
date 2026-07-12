package zm.unza.tels.cemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.exception.ResourceNotFoundException;
import zm.unza.tels.cemis.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public User create(String email, String tempPassword, String fullName, User.AppRole role) {
        if (userRepository.existsByEmail(email))
            throw new IllegalStateException("Email already registered: " + email);
        var user = User.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(tempPassword))
            .fullName(fullName)
            .role(role)
            .mustChangePassword(true)
            .build();
        return userRepository.save(user);
    }

    @Transactional
    public User update(UUID id, String fullName, User.AppRole role, Boolean active) {
        var user = getById(id);
        if (fullName != null) user.setFullName(fullName);
        if (role     != null) user.setRole(role);
        if (active   != null) user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.delete(getById(id));
    }
}
