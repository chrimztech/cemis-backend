package zm.unza.tels.cemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.tels.cemis.entity.Course;
import zm.unza.tels.cemis.exception.ResourceNotFoundException;
import zm.unza.tels.cemis.repository.CourseRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<Course> listAll()    { return courseRepository.findAllByOrderByNameAsc(); }
    public List<Course> listActive() { return courseRepository.findByActiveTrueOrderByNameAsc(); }

    public Course getById(UUID id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    @Transactional
    public Course create(Course course) {
        if (courseRepository.existsByCode(course.getCode()))
            throw new IllegalStateException("Course code already exists: " + course.getCode());
        return courseRepository.save(course);
    }

    @Transactional
    public Course update(UUID id, Course patch) {
        var existing = getById(id);
        if (patch.getName()         != null) existing.setName(patch.getName());
        if (patch.getDescription()  != null) existing.setDescription(patch.getDescription());
        if (patch.getPrefix()       != null) existing.setPrefix(patch.getPrefix());
        if (patch.getMode()         != null) existing.setMode(patch.getMode());
        if (patch.getDurationText() != null) existing.setDurationText(patch.getDurationText());
        if (patch.getFeeUnza()      != null) existing.setFeeUnza(patch.getFeeUnza());
        if (patch.getFeeNonUnza()   != null) existing.setFeeNonUnza(patch.getFeeNonUnza());
        if (patch.getStartDate()    != null) existing.setStartDate(patch.getStartDate());
        if (patch.getTimeSlot()     != null) existing.setTimeSlot(patch.getTimeSlot());
        existing.setActive(patch.isActive());
        return courseRepository.save(existing);
    }

    @Transactional
    public void delete(UUID id) {
        courseRepository.delete(getById(id));
    }
}
