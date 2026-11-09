package com.tallertech.app.data.sync.mapper;

import com.tallertech.app.data.local.entity.ServiceOrderEntity;
import com.tallertech.app.data.remote.dto.orders.*;
import com.tallertech.app.data.sync.SyncStatus;

public final class ServiceOrderMapper {
    private ServiceOrderMapper() {}
    public static ServiceOrderRequest request(ServiceOrderEntity entity) {
        ServiceOrderRequest dto = new ServiceOrderRequest();
        dto.id = entity.getId();
        dto.code = entity.getCode();
        dto.clientId = entity.getClientId();
        dto.equipmentId = entity.getEquipmentId();
        dto.issue = entity.getIssue();
        dto.entryDate = entity.getEntryDate();
        dto.technician = entity.getTechnician();
        dto.status = entity.getStatus();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
    public static ServiceOrderEntity entity(ServiceOrderResponse dto) {
        if (dto.id == null || dto.updatedAt < 0 || dto.clientId == null || dto.equipmentId == null || dto.entryDate == null)
            throw new IllegalArgumentException("Respuesta remota inválida: ServiceOrder");
        java.util.UUID.fromString(dto.id);
        return new ServiceOrderEntity(dto.id, dto.code, dto.clientId, dto.equipmentId, dto.issue, dto.entryDate, dto.technician, dto.status, dto.updatedAt, SyncStatus.SYNCED);
    }
}
