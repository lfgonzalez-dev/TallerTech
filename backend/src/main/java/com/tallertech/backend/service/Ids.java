package com.tallertech.backend.service;

import java.util.UUID;

public final class Ids {
    public static final String UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private Ids() {}

    public static void validate(String id) {
        if (id == null || !id.matches(UUID_PATTERN)) throw new IllegalArgumentException("ID UUID inválido");
    }

    public static String forCreate(String id) {
        if (id == null) return UUID.randomUUID().toString();
        validate(id);
        return id;
    }

    public static void checkUpdate(String pathId, String bodyId) {
        validate(pathId);
        if (bodyId != null && !pathId.equals(bodyId)) throw new IllegalArgumentException("ID del cuerpo diferente al de la ruta");
    }
}

