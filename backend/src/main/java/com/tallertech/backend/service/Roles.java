package com.tallertech.backend.service;

import java.text.Normalizer;
import java.util.Locale;

public final class Roles {
    private Roles() {}
    public static String normalize(String role) {
        String value = Normalizer.normalize(role == null ? "" : role, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT);
        if (value.equals("ADMINISTRADOR")) return "ADMIN";
        return value;
    }
}
