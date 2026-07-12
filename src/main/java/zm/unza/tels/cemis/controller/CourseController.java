package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
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
    public List<Course> list(@RequestParam(required = false) Boolean active) {
        return Boolean.TRUE.equals(active) ? courseService.listActive() : courseService.listAll();
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
