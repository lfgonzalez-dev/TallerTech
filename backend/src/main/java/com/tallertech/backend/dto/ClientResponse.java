package com.tallertech.backend.dto;

import com.tallertech.backend.entity.Client;

public record ClientResponse(String id, String name, String phone, String email, String address, String notes, long updatedAt) {
    public static ClientResponse from(Client entity) {
        return new ClientResponse(entity.getId(), entity.getName(), entity.getPhone(), entity.getEmail(), entity.getAddress(), entity.getNotes(), entity.getUpdatedAt());
    }
}

