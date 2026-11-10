package com.tallertech.app.data.sync.mapper;

import com.tallertech.app.data.local.entity.RepairEntity;
import com.tallertech.app.data.remote.dto.repairs.*;
import com.tallertech.app.data.sync.SyncStatus;

public final class RepairMapper {
    private RepairMapper() {}
    public static RepairRequest request(RepairEntity entity) {
        RepairRequest dto = new RepairRequest();
        dto.id = entity.getId();
        dto.orderId = entity.getOrderId();
        dto.repair = entity.getRepair();
        dto.parts = entity.getParts();
        dto.observations = entity.getObservations();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
    public static RepairEntity entity(RepairResponse dto) {
        if (dto.id == null || dto.updatedAt < 0 || dto.orderId == null)
            throw new IllegalArgumentException("Respuesta remota inválida: Repair");
        java.util.UUID.fromString(dto.id);
        return new RepairEntity(dto.id, dto.orderId, dto.repair, dto.parts, dto.observations, dto.updatedAt, SyncStatus.SYNCED);
    }
}
