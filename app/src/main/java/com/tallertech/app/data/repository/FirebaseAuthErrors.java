package com.tallertech.app.data.repository;

/** Public messages only; never display SDK exception details or account existence. */
public final class FirebaseAuthErrors {
    private FirebaseAuthErrors() {}

    public static String message(String code) {
        if (code == null) return "No se pudo iniciar sesión. Intenta nuevamente.";
        switch (code) {
            case "ERROR_INVALID_EMAIL": return "Ingresa un correo electrónico válido";
            case "ERROR_USER_DISABLED": return "Esta cuenta está deshabilitada";
            case "ERROR_INVALID_USER_TOKEN":
            case "ERROR_USER_TOKEN_EXPIRED": return "Tu sesión expiró. Inicia sesión nuevamente.";
            case "ERROR_USER_NOT_FOUND":
            case "ERROR_WRONG_PASSWORD":
            case "ERROR_INVALID_CREDENTIAL":
            case "ERROR_INVALID_LOGIN_CREDENTIALS": return "Correo o contraseña incorrectos";
            case "ERROR_NETWORK_REQUEST_FAILED": return "No se pudo conectar. Revisa tu conexión a Internet.";
            case "ERROR_TOO_MANY_REQUESTS": return "Demasiados intentos. Intenta más tarde.";
            case "ERROR_OPERATION_NOT_ALLOWED": return "El acceso por correo y contraseña no está disponible";
            default: return "No se pudo iniciar sesión. Intenta nuevamente.";
        }
    }
}
