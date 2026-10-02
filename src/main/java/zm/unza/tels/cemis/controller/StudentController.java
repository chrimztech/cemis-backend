package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zm.unza.tels.cemis.entity.Student;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.security.CemisUserDetails;
import zm.unza.tels.cemis.service.StudentService;
import zm.unza.tels.cemis.repository.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService  studentService;
    private final UserRepository  userRepository;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String nationalId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (nationalId != null) {
            return studentService.findByNationalId(nationalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
        }
        if (page != null) {
            return ResponseEntity.ok(studentService.search(q, category, page, size == null ? 25 : size));
        }
        return ResponseEntity.ok(studentService.listAll());
    }

    @GetMapping("/{id}")
    public Student get(@PathVariable UUID id) {
        return studentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Student> create(@RequestBody Student student,
                                          @AuthenticationPrincipal CemisUserDetails principal) {
        return ResponseEntity.ok(studentService.create(student, actor(principal)));
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable UUID id, @RequestBody Student patch,
                          @AuthenticationPrincipal CemisUserDetails principal) {
        return studentService.update(id, patch, actor(principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id,
                                                      @AuthenticationPrincipal CemisUserDetails principal) {
        studentService.delete(id, actor(principal));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCsv(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CemisUserDetails principal) throws Exception {
        var imported = studentService.importCsv(file, actor(principal));
        return ResponseEntity.ok(Map.of("imported", imported.size(), "students", imported));
    }

    private User actor(CemisUserDetails p) {
        return userRepository.findById(p.getId()).orElse(null);
    }
}
