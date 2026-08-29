package zm.unza.tels.cemis.service;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import zm.unza.tels.cemis.entity.Student;
import zm.unza.tels.cemis.entity.StudentAccessLog;
import zm.unza.tels.cemis.entity.User;
import zm.unza.tels.cemis.exception.ResourceNotFoundException;
import zm.unza.tels.cemis.repository.StudentAccessLogRepository;
import zm.unza.tels.cemis.repository.StudentRepository;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository       studentRepository;
    private final StudentAccessLogRepository logRepository;

    @Transactional(readOnly = true)
    public List<Student> listAll() {
        return studentRepository.findAllByOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public Page<Student> search(String query, int page, int size) {
        return studentRepository.search(query, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Optional<Student> findByNationalId(String nationalId) {
        return studentRepository.findByNationalId(nationalId);
    }

    @Transactional(readOnly = true)
    public Student getById(UUID id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    @Transactional
    public Student create(Student student, User actor) {
        var saved = studentRepository.save(student);
        log(saved, actor, "created", null);
        return saved;
    }

    @Transactional
    public Student update(UUID id, Student patch, User actor) {
        var existing = getById(id);
        if (patch.getFullName()     != null) existing.setFullName(patch.getFullName());
        if (patch.getEmail()        != null) existing.setEmail(patch.getEmail());
        if (patch.getPhone()        != null) existing.setPhone(patch.getPhone());
        if (patch.getNationalId()   != null) existing.setNationalId(patch.getNationalId());
        if (patch.getUnzaStudentId()!= null) existing.setUnzaStudentId(patch.getUnzaStudentId());
        if (patch.getCategory()     != null) existing.setCategory(patch.getCategory());
        if (patch.getNotes()        != null) existing.setNotes(patch.getNotes());
        var saved = studentRepository.save(existing);
        log(saved, actor, "updated", null);
        return saved;
    }

    @Transactional
    public void delete(UUID id, User actor) {
        var s = getById(id);
        log(s, actor, "deleted", null);
        studentRepository.delete(s);
    }

    @Transactional
    public List<Student> importCsv(MultipartFile file, User actor) throws Exception {
        var imported = new ArrayList<Student>();
        try (var reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (header == null) throw new IllegalArgumentException("CSV file is empty");

            // Resolve column indexes by header name (case-insensitive)
            int iName = indexOf(header, "full_name", "name");
            int iEmail = indexOf(header, "email");
            int iPhone = indexOf(header, "phone");
            int iNrc  = indexOf(header, "national_id", "nrc");
            int iUnza = indexOf(header, "unza_student_id", "unza_id");
            int iCat  = indexOf(header, "category");

            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length == 0 || row[0].isBlank()) continue;
                var s = Student.builder()
                    .fullName(iName  >= 0 ? row[iName].trim()  : "")
                    .email(   iEmail >= 0 ? row[iEmail].trim() : null)
                    .phone(   iPhone >= 0 ? row[iPhone].trim() : null)
                    .nationalId(iNrc >= 0 ? row[iNrc].trim()  : null)
                    .unzaStudentId(iUnza >= 0 ? row[iUnza].trim() : null)
                    .category(iCat >= 0 && !row[iCat].isBlank() ? row[iCat].trim().toLowerCase() : "non_unza")
                    .build();
                if (s.getFullName().isBlank()) continue;
                var saved = studentRepository.save(s);
                log(saved, actor, "csv_import", null);
                imported.add(saved);
            }
        }
        return imported;
    }

    private int indexOf(String[] header, String... candidates) {
        for (int i = 0; i < header.length; i++) {
            for (String c : candidates) {
                if (header[i].trim().equalsIgnoreCase(c)) return i;
            }
        }
        return -1;
    }

    private void log(Student s, User actor, String action, String detail) {
        logRepository.save(StudentAccessLog.builder()
            .student(s).actor(actor).action(action).detail(detail).build());
    }
}
