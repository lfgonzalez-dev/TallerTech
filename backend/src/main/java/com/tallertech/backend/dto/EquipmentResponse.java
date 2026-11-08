package com.tallertech.backend.dto;

import com.tallertech.backend.entity.Equipment;

public record EquipmentResponse(String id, String clientId, String type, String brand, String model, String serial, String description, long updatedAt) {
    public static EquipmentResponse from(Equipment entity) {
        return new EquipmentResponse(entity.getId(), entity.getClientId(), entity.getType(), entity.getBrand(), entity.getModel(), entity.getSerial(), entity.getDescription(), entity.getUpdatedAt());
    }
}

