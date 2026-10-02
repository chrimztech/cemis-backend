package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.tels.cemis.entity.Course;
import zm.unza.tels.cemis.service.CourseService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null) {
            Page<Course> result = courseService.search(
                q, Boolean.TRUE.equals(active), category, page, size == null ? 25 : size);
            return ResponseEntity.ok(result);
        }
        List<Course> result = Boolean.TRUE.equals(active) ? courseService.listActive() : courseService.listAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable UUID id) {
        return courseService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Course> create(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.create(course));
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable UUID id, @RequestBody Course patch) {
        return courseService.update(id, patch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
