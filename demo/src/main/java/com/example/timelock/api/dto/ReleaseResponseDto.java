package com.example.timelock.api.dto;

import com.example.timelock.release.Release;
import com.example.timelock.release.ReleaseStatus;
import java.time.Instant;

public record ReleaseResponseDto(
    Long id,
    String title,
    String description,
    String payloadJson,
    ReleaseStatus status,
    Instant scheduledAt,
    String createdBy,
    Instant createdAt,
    String approvedBy,
    Instant approvedAt,
    Instant executedAt
) {
    public static ReleaseResponseDto from(Release release) {
        return new ReleaseResponseDto(
            release.getId(),
            release.getTitle(),
            release.getDescription(),
            release.getPayloadJson(),
            release.getStatus(),
            release.getScheduledAt(),
            release.getCreatedBy(),
            release.getCreatedAt(),
            release.getApprovedBy(),
            release.getApprovedAt(),
            release.getExecutedAt()
        );
    }
}


