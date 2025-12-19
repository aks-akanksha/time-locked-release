package com.example.timelock.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record ScheduleDto(
    @NotNull(message = "scheduledAt is required")
    OffsetDateTime scheduledAt
) {}
