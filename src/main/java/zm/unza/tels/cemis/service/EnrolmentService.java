package zm.unza.tels.cemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    public List<Enrolment> listAll() {
        return enrolmentRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public Page<Enrolment> listFilteredPaged(List<String> statusIn, Boolean noCertificate, int page, int size) {
        List<Enrolment.EnrolStatus> statuses = (statusIn == null ? List.<String>of() : statusIn).stream()
            .map(Enrolment.EnrolStatus::valueOf).toList();
        return enrolmentRepository.findByStatusInAndNoCertificate(
            statuses, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }

    // Postgres can't infer a bind parameter's type from a bare "? IS NULL" check on a
    // timestamp column, so absent date bounds use these sentinels instead of NULL.
    private static final Instant MIN_DATE = Instant.EPOCH;
    private static final Instant MAX_DATE = Instant.parse("9999-12-31T23:59:59Z");

    @Transactional(readOnly = true)
    public Page<Enrolment> search(
            String q, UUID courseId, Enrolment.EnrolStatus status,
            Instant fromDate, Instant toDate, int page, int size) {
        return enrolmentRepository.search(
            q == null ? "" : q, courseId, status != null ? status.name() : null,
            fromDate != null ? fromDate : MIN_DATE,
            toDate != null ? toDate : MAX_DATE,
            PageRequest.of(page, size, Sort.by("enrolledAt").descending())
        );
    }

    @Transactional(readOnly = true)
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
        return bulkUpdateStatus(ids, Enrolment.EnrolStatus.in_progress);
    }

    @Transactional
    public int bulkComplete(List<UUID> ids) {
        return bulkUpdateStatus(ids, Enrolment.EnrolStatus.completed);
    }

    private int bulkUpdateStatus(List<UUID> ids, Enrolment.EnrolStatus status) {
        int count = 0;
        for (UUID id : ids) {
            try {
                updateStatus(id, status);
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
