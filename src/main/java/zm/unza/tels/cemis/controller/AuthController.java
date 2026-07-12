package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.security.CemisUserDetails;
import zm.unza.tels.cemis.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        try {
            var result = authService.login(body.get("email"), body.get("password"));
            return ResponseEntity.ok(Map.of(
                "token",              result.token(),
                "id",                 result.user().getId(),
                "email",              result.user().getEmail(),
                "fullName",           result.user().getFullName() != null ? result.user().getFullName() : "",
                "role",               result.user().getRole().name(),
                "mustChangePassword", result.user().isMustChangePassword()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal CemisUserDetails principal) {
        User user = authService.getMe(principal.getId());
        return ResponseEntity.ok(Map.of(
            "id",                 user.getId(),
            "email",              user.getEmail(),
            "fullName",           user.getFullName() != null ? user.getFullName() : "",
            "role",               user.getRole().name(),
            "mustChangePassword", user.isMustChangePassword()
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal CemisUserDetails principal,
            @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters"));
        authService.changePassword(principal.getId(), newPassword);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
