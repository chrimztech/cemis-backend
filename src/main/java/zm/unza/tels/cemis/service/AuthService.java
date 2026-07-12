package zm.unza.tels.cemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.exception.ResourceNotFoundException;
import zm.unza.tels.cemis.repository.UserRepository;
import zm.unza.tels.cemis.security.CemisUserDetails;
import zm.unza.tels.cemis.security.JwtTokenProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider      tokenProvider;
    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;

    public record LoginResult(String token, User user) {}

    public LoginResult login(String email, String password) {
        var auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));
        String token = tokenProvider.generate(auth);
        var details = (CemisUserDetails) auth.getPrincipal();
        var user = userRepository.findById(details.getId()).orElseThrow();
        return new LoginResult(token, user);
    }

    public User getMe(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void changePassword(UUID userId, String newPassword) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }
}
