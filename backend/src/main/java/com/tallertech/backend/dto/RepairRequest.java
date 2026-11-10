package com.tallertech.backend.dto;

import com.tallertech.backend.service.Ids;
import jakarta.validation.constraints.*;

public record RepairRequest(
        @Pattern(regexp = Ids.UUID_PATTERN) String id,
        @NotBlank @Pattern(regexp = Ids.UUID_PATTERN) String orderId,
        @Size(max = 10000) String repair,
        @Size(max = 10000) String parts,
        @Size(max = 10000) String observations,
        @PositiveOrZero Long updatedAt
) {}


