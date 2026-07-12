package zm.unza.tels.cemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zm.unza.tels.cemis.entity.Enrolment;
import zm.unza.tels.cemis.exception.ResourceNotFoundException;
import zm.unza.tels.cemis.repository.CourseRepository;
import zm.unza.tels.cemis.repository.EnrolmentRepository;
import zm.unza.tels.cemis.repository.StudentRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrolmentService {

    private final EnrolmentRepository enrolmentRepository;
    private final StudentRepository   studentRepository;
    private final CourseRepository    courseRepository;

    public List<Enrolment> listAll() {
        return enrolmentRepository.findAllWithDetails();
    }

    public List<Enrolment> listFiltered(UUID studentId, List<String> statusIn, Boolean noCertificate) {
        if (studentId != null) {
            return enrolmentRepository.findByStudentIdWithDetails(studentId);
        }
        if (statusIn != null && !statusIn.isEmpty() && Boolean.TRUE.equals(noCertificate)) {
            List<Enrolment.EnrolStatus> statuses = statusIn.stream()
                .map(Enrolment.EnrolStatus::valueOf).toList();
            return enrolmentRepository.findByStatusInAndNoCertificate(statuses);
        }
        return enrolmentRepository.findAllWithDetails();
    }

    public Enrolment getById(UUID id) {
        return enrolmentRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Enrolment not found: " + id));
    }

    @Transactional
    public Enrolment create(UUID studentId, UUID courseId,
                            BigDecimal feeCharged, Enrolment.PayStatus paymentStatus) {
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        var course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (enrolmentRepository.existsByStudentIdAndCourseId(studentId, courseId))
            throw new IllegalStateException("Student is already enrolled in this course");

        // Auto-suggest fee if not provided
        BigDecimal fee = feeCharged;
        if (fee == null) {
            fee = "unza".equals(student.getCategory()) ? course.getFeeUnza() : course.getFeeNonUnza();
        }

        // If fee is zero, mark as free automatically
        if (paymentStatus == null) {
            paymentStatus = (fee != null && fee.compareTo(BigDecimal.ZERO) == 0)
                ? Enrolment.PayStatus.free : Enrolment.PayStatus.pending;
        }

        var enrolment = Enrolment.builder()
            .student(student).course(course)
            .feeCharged(fee).paymentStatus(paymentStatus)
            .build();
        return enrolmentRepository.save(enrolment);
    }

    @Transactional
    public Enrolment updateStatus(UUID id, Enrolment.EnrolStatus status) {
        var e = getById(id);
        e.setStatus(status);
        if (status == Enrolment.EnrolStatus.in_progress && e.getStartedAt() == null)
            e.setStartedAt(Instant.now());
        if (status == Enrolment.EnrolStatus.completed && e.getCompletedAt() == null)
            e.setCompletedAt(Instant.now());
        return enrolmentRepository.save(e);
    }

    @Transactional
    public Enrolment updatePaymentStatus(UUID id, Enrolment.PayStatus payStatus) {
        var e = getById(id);
        e.setPaymentStatus(payStatus);
        return enrolmentRepository.save(e);
    }

    @Transactional
    public int bulkStart(List<UUID> ids) {
        int count = 0;
        for (UUID id : ids) {
            try {
                updateStatus(id, Enrolment.EnrolStatus.in_progress);
                count++;
            } catch (Exception ignored) {}
        }
        return count;
    }

    @Transactional
    public Enrolment patch(UUID id, java.util.Map<String, Object> fields) {
        var e = getById(id);
        if (fields.containsKey("notes")) e.setNotes((String) fields.get("notes"));
        if (fields.containsKey("fee_charged") && fields.get("fee_charged") != null)
            e.setFeeCharged(new java.math.BigDecimal(fields.get("fee_charged").toString()));
        return enrolmentRepository.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        enrolmentRepository.delete(getById(id));
    }
}
