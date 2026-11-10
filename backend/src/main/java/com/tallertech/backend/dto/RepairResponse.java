package com.tallertech.backend.dto;

import com.tallertech.backend.entity.Repair;

public record RepairResponse(String id, String orderId, String repair, String parts, String observations, long updatedAt) {
    public static RepairResponse from(Repair entity) {
        return new RepairResponse(entity.getId(), entity.getOrderId(), entity.getRepair(), entity.getParts(), entity.getObservations(), entity.getUpdatedAt());
    }
}

