package com.tallertech.app.data.remote;
import java.net.SocketTimeoutException;
import retrofit2.Response;

public final class ApiErrors {
    private ApiErrors() {}
    public static String message(Response<?> response) {
        switch (response.code()) {
            case 400: return "Revisa los datos ingresados";
            case 401: return "Sesión inválida o expirada";
            case 403: return "No tienes permiso para esta operación";
            case 404: return "Recurso no encontrado";
            case 409: return "Los datos entran en conflicto con un registro existente";
            default: return response.code() >= 500 ? "Error del servidor. Intenta más tarde" : "No se pudo completar la solicitud";
        }
    }
    public static String message(Throwable error) {
        return error instanceof SocketTimeoutException ? "El servidor tardó demasiado en responder" : "No se pudo conectar con el servidor";
    }
}
