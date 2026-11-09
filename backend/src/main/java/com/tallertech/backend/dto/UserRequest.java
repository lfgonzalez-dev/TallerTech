package com.tallertech.backend.dto;

import com.tallertech.backend.service.Ids;
import jakarta.validation.constraints.*;

public record UserRequest(
        @Pattern(regexp = Ids.UUID_PATTERN) String id,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 255) String role,
        @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
        @Size(min = 12, max = 72) String password,
        @PositiveOrZero Long updatedAt
) {}


