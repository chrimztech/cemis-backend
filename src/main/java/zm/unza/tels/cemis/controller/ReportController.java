package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zm.unza.tels.cemis.dto.AuditLogEntryDto;
import zm.unza.tels.cemis.entity.StudentAccessLog;
import zm.unza.tels.cemis.repository.StudentAccessLogRepository;
import zm.unza.tels.cemis.repository.StudentRepository;
import zm.unza.tels.cemis.repository.UserRepository;
import zm.unza.tels.cemis.security.CemisUserDetails;
import zm.unza.tels.cemis.service.ReportService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService              reportService;
    private final StudentAccessLogRepository auditLogRepository;
    private final StudentRepository          studentRepository;
    private final UserRepository             userRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(reportService.overviewStats());
    }

    @GetMapping("/audit-log")
    public ResponseEntity<List<AuditLogEntryDto>> auditLog(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "500") int size) {
        var result = auditLogRepository.findAllWithDetails(
            PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<AuditLogEntryDto> dtos = result.getContent().stream()
            .map(AuditLogEntryDto::from).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/audit-log")
    public ResponseEntity<Map<String, Object>> createAuditLog(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CemisUserDetails principal) {
        var log = StudentAccessLog.builder()
            .action(body.getOrDefault("action", "view").toString())
            .detail(body.get("detail") != null ? body.get("detail").toString() : null)
            .build();

        if (body.get("student_id") != null) {
            try {
                UUID studentId = UUID.fromString(body.get("student_id").toString());
                studentRepository.findById(studentId).ifPresent(log::setStudent);
            } catch (IllegalArgumentException ignored) {}
        }
        if (principal != null) {
            userRepository.findById(principal.getId()).ifPresent(log::setActor);
        }
        auditLogRepository.save(log);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
