package com.tallertech.backend.dto;

import com.tallertech.backend.entity.Diagnosis;

public record DiagnosisResponse(String id, String orderId, String diagnosis, String observations, String recommendations, long updatedAt) {
    public static DiagnosisResponse from(Diagnosis entity) {
        return new DiagnosisResponse(entity.getId(), entity.getOrderId(), entity.getDiagnosis(), entity.getObservations(), entity.getRecommendations(), entity.getUpdatedAt());
    }
}

