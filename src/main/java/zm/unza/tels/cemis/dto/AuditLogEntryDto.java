package zm.unza.tels.cemis.dto;

import zm.unza.tels.cemis.entity.StudentAccessLog;

import java.time.Instant;
import java.util.UUID;

public record AuditLogEntryDto(
        UUID id,
        String action,
        UUID actorId,
        String actorEmail,
        String detail,
        Instant createdAt,
        UUID studentId,
        StudentSummary students
) {
    public record StudentSummary(String fullName, String email) {}

    public static AuditLogEntryDto from(StudentAccessLog log) {
        StudentSummary studentSummary = null;
        UUID studentId = null;
        if (log.getStudent() != null) {
            studentId = log.getStudent().getId();
            studentSummary = new StudentSummary(
                log.getStudent().getFullName(),
                log.getStudent().getEmail()
            );
        }
        String actorEmail = null;
        UUID actorId = null;
        if (log.getActor() != null) {
            actorId = log.getActor().getId();
            actorEmail = log.getActor().getEmail();
        }
        return new AuditLogEntryDto(
            log.getId(), log.getAction(), actorId, actorEmail,
            log.getDetail(), log.getCreatedAt(), studentId, studentSummary
        );
    }
}
