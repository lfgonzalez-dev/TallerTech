package com.tallertech.backend.dto;

import com.tallertech.backend.service.Ids;
import jakarta.validation.constraints.*;

public record UserRequest(
        @Pattern(regexp = Ids.UUID_PATTERN) String id,
        @Size(max = 255) String name,
        @Email @Size(max = 255) String email,
        @Size(max = 255) String role,
        @PositiveOrZero Long updatedAt
) {}


