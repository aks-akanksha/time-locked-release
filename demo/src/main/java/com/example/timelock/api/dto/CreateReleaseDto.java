package com.example.timelock.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReleaseDto(
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    String title,
    
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,
    
    @Size(max = 10000, message = "Payload JSON must not exceed 10000 characters")
    String payloadJson
) {}
