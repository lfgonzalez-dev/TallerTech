package com.tallertech.backend.dto;

import com.tallertech.backend.entity.ServiceOrder;

public record ServiceOrderResponse(String id, String code, String clientId, String equipmentId, String issue, Long entryDate, String technician, String status, long updatedAt) {
    public static ServiceOrderResponse from(ServiceOrder entity) {
        return new ServiceOrderResponse(entity.getId(), entity.getCode(), entity.getClientId(), entity.getEquipmentId(), entity.getIssue(), entity.getEntryDate(), entity.getTechnician(), entity.getStatus(), entity.getUpdatedAt());
    }
}

