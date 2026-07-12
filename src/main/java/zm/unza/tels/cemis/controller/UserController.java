package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> list() {
        return userService.listAll();
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody Map<String, String> body) {
        User.AppRole role = body.get("role") != null
            ? User.AppRole.valueOf(body.get("role")) : User.AppRole.user;
        return ResponseEntity.ok(userService.create(
            body.get("email"), body.get("password"), body.get("fullName"), role));
    }

    @PutMapping("/{id}")
    public User update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        User.AppRole role = body.get("role") != null
            ? User.AppRole.valueOf(body.get("role").toString()) : null;
        Boolean active = body.get("active") != null
            ? Boolean.valueOf(body.get("active").toString()) : null;
        return userService.update(id, (String) body.get("fullName"), role, active);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
