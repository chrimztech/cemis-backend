package zm.unza.tels.cemis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zm.unza.tels.cemis.entity.Enrolment;
import zm.unza.tels.cemis.service.EnrolmentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/enrolments")
@RequiredArgsConstructor
public class EnrolmentController {

    private final EnrolmentService enrolmentService;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) List<String> statusIn,
            @RequestParam(required = false) Boolean noCertificate,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null) {
            if (studentId == null && Boolean.TRUE.equals(noCertificate) && statusIn != null && !statusIn.isEmpty()) {
                return ResponseEntity.ok(
                    enrolmentService.listFilteredPaged(statusIn, noCertificate, page, size == null ? 25 : size)
                );
            }
            Enrolment.EnrolStatus statusEnum = status != null ? Enrolment.EnrolStatus.valueOf(status) : null;
            Instant from = fromDate != null ? LocalDate.parse(fromDate).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
            Instant to = toDate != null ? LocalDate.parse(toDate).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant() : null;
            return ResponseEntity.ok(
                enrolmentService.search(q, courseId, statusEnum, from, to, page, size == null ? 25 : size)
            );
        }
        return ResponseEntity.ok(enrolmentService.listFiltered(studentId, statusIn, noCertificate));
    }

    @GetMapping("/{id}")
    public Enrolment get(@PathVariable UUID id) {
        return enrolmentService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Enrolment> create(@RequestBody Map<String, Object> body) {
        UUID studentId     = UUID.fromString(body.get("student_id").toString());
        UUID courseId      = UUID.fromString(body.get("course_id").toString());
        BigDecimal fee     = body.get("fee_charged") != null
            ? new BigDecimal(body.get("fee_charged").toString()) : null;
        Enrolment.PayStatus pay = body.get("payment_status") != null
            ? Enrolment.PayStatus.valueOf(body.get("payment_status").toString()) : null;
        return ResponseEntity.ok(enrolmentService.create(studentId, courseId, fee, pay));
    }

    @PatchMapping("/{id}/status")
    public Enrolment updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return enrolmentService.updateStatus(id, Enrolment.EnrolStatus.valueOf(body.get("status")));
    }

    @PatchMapping("/{id}/payment")
    public Enrolment updatePayment(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return enrolmentService.updatePaymentStatus(id, Enrolment.PayStatus.valueOf(body.get("payment_status")));
    }

    @PatchMapping("/{id}")
    public Enrolment patch(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return enrolmentService.patch(id, body);
    }

    @PostMapping("/bulk-start")
    public ResponseEntity<Map<String, Object>> bulkStart(@RequestBody Map<String, List<String>> body) {
        List<UUID> ids = body.get("ids").stream().map(UUID::fromString).toList();
        int updated = enrolmentService.bulkStart(ids);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @PostMapping("/bulk-complete")
    public ResponseEntity<Map<String, Object>> bulkComplete(@RequestBody Map<String, List<String>> body) {
        List<UUID> ids = body.get("ids").stream().map(UUID::fromString).toList();
        int updated = enrolmentService.bulkComplete(ids);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable UUID id) {
        enrolmentService.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
