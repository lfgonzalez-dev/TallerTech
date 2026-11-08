package com.tallertech.backend.dto;

import com.tallertech.backend.service.Ids;
import jakarta.validation.constraints.*;

public record ServiceOrderRequest(
        @Pattern(regexp = Ids.UUID_PATTERN) String id,
        @NotBlank @Size(max = 255) String code,
        @NotBlank @Pattern(regexp = Ids.UUID_PATTERN) String clientId,
        @NotBlank @Pattern(regexp = Ids.UUID_PATTERN) String equipmentId,
        @Size(max = 10000) String issue,
        @NotNull @PositiveOrZero Long entryDate,
        @Size(max = 255) String technician,
        @NotBlank @Size(max = 255) @Pattern(regexp = "Pendiente|Diagnóstico|En reparación|Completado") String status,
        @PositiveOrZero Long updatedAt
) {}


