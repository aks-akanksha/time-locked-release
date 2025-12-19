package com.example.timelock.api.dto;

import com.example.timelock.audit.ReleaseAuditLog;
import java.time.Instant;

public record AuditLogDto(
    Long id,
    Long releaseId,
    String action,
    String performedBy,
    Instant performedAt,
    String details
) {
    public static AuditLogDto from(ReleaseAuditLog log) {
        return new AuditLogDto(
            log.getId(),
            log.getReleaseId(),
            log.getAction(),
            log.getPerformedBy(),
            log.getPerformedAt(),
            log.getDetails()
        );
    }
}

