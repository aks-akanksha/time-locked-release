package com.example.timelock.api.dto;

public record CreateReleaseDto(
    String title,
    String description,
    String payloadJson
) {}
