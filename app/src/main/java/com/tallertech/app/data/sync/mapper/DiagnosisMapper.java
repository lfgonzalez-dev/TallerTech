package com.tallertech.app.data.sync.mapper;

import com.tallertech.app.data.local.entity.DiagnosisEntity;
import com.tallertech.app.data.remote.dto.diagnoses.*;
import com.tallertech.app.data.sync.SyncStatus;

public final class DiagnosisMapper {
    private DiagnosisMapper() {}
    public static DiagnosisRequest request(DiagnosisEntity entity) {
        DiagnosisRequest dto = new DiagnosisRequest();
        dto.id = entity.getId();
        dto.orderId = entity.getOrderId();
        dto.diagnosis = entity.getDiagnosis();
        dto.observations = entity.getObservations();
        dto.recommendations = entity.getRecommendations();
        dto.updatedAt = entity.getUpdatedAt();
        return dto;
    }
    public static DiagnosisEntity entity(DiagnosisResponse dto) {
        if (dto.id == null || dto.updatedAt < 0 || dto.orderId == null)
            throw new IllegalArgumentException("Respuesta remota inválida: Diagnosis");
        java.util.UUID.fromString(dto.id);
        return new DiagnosisEntity(dto.id, dto.orderId, dto.diagnosis, dto.observations, dto.recommendations, dto.updatedAt, SyncStatus.SYNCED);
    }
}
