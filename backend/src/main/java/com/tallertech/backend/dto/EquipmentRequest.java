package com.tallertech.backend.dto;

import com.tallertech.backend.service.Ids;
import jakarta.validation.constraints.*;

public record EquipmentRequest(
        @Pattern(regexp = Ids.UUID_PATTERN) String id,
        @NotBlank @Pattern(regexp = Ids.UUID_PATTERN) String clientId,
        @Size(max = 255) String type,
        @Size(max = 255) String brand,
        @Size(max = 255) String model,
        @Size(max = 255) String serial,
        @Size(max = 10000) String description,
        @PositiveOrZero Long updatedAt
) {}


