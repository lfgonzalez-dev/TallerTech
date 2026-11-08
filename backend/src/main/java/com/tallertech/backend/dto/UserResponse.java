package com.tallertech.backend.dto;

import com.tallertech.backend.entity.User;

public record UserResponse(String id, String name, String email, String role, long updatedAt) {
    public static UserResponse from(User entity) {
        return new UserResponse(entity.getId(), entity.getName(), entity.getEmail(), entity.getRole(), entity.getUpdatedAt());
    }
}

