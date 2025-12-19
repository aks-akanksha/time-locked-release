package com.example.timelock.api.dto;

import java.util.Map;

public record ReleaseStatisticsDto(
    long totalReleases,
    Map<String, Long> releasesByStatus,
    long scheduledReleases,
    long approvedReleases,
    long executedReleases,
    long cancelledReleases
) {}

