package com.tallertech.app.data.sync.mapper;

import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.remote.dto.equipment.*;
import com.tallertech.app.data.sync.SyncStatus;

public final class EquipmentMapper {
    private EquipmentMapper() {}
    public static EquipmentRequest request(EquipmentEntity entity) {
        EquipmentRequest dto = new EquipmentRequest();
        dto.id = entity.getId();
        dto.clientId = entity.getClientId();
        dto.type = entity.getType();
        dto.brand = entity.getBrand();
        dto.model = entity.getModel();
        dto.serial = entity.getSerial();
        dto.description = entity.getDescription();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
    public static EquipmentEntity entity(EquipmentResponse dto) {
        if (dto.id == null || dto.updatedAt < 0 || dto.clientId == null)
            throw new IllegalArgumentException("Respuesta remota inválida: Equipment");
        java.util.UUID.fromString(dto.id);
        return new EquipmentEntity(dto.id, dto.clientId, dto.type, dto.brand, dto.model, dto.serial, dto.description, dto.updatedAt, SyncStatus.SYNCED);
    }
}
