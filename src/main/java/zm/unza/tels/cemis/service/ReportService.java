package zm.unza.tels.cemis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zm.unza.tels.cemis.entity.Certificate;
import zm.unza.tels.cemis.entity.Enrolment;
import zm.unza.tels.cemis.repository.CertificateRepository;
import zm.unza.tels.cemis.repository.CourseRepository;
import zm.unza.tels.cemis.repository.EnrolmentRepository;
import zm.unza.tels.cemis.repository.StudentRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudentRepository    studentRepository;
    private final CourseRepository     courseRepository;
    private final EnrolmentRepository  enrolmentRepository;
    private final CertificateRepository certRepository;

    public Map<String, Object> overviewStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents",    studentRepository.count());
        stats.put("totalCourses",     courseRepository.count());
        stats.put("totalEnrolments",  enrolmentRepository.count());
        stats.put("totalCertificates", certRepository.count());
        stats.put("enrolled",         enrolmentRepository.countByStatus(Enrolment.EnrolStatus.enrolled));
        stats.put("inProgress",       enrolmentRepository.countByStatus(Enrolment.EnrolStatus.in_progress));
        stats.put("completed",        enrolmentRepository.countByStatus(Enrolment.EnrolStatus.completed));
        stats.put("certified",        enrolmentRepository.countByStatus(Enrolment.EnrolStatus.certified));
        stats.put("certsValid",       certRepository.countByStatus(Certificate.CertStatus.valid));
        stats.put("certsRevoked",     certRepository.countByStatus(Certificate.CertStatus.revoked));
        stats.put("certsSent",        certRepository.countByEmailStatus("sent"));
        stats.put("certsPending",     certRepository.countByEmailStatus("not_sent"));
        return stats;
    }
}
