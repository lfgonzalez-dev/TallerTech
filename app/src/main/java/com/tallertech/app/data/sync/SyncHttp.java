package com.tallertech.app.data.sync;

import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;

final class SyncHttp {
    static final class Failure extends IOException {
        final int code;
        Failure(int code) { super("HTTP " + code); this.code = code; }
        boolean retryable() { return code == 408 || code == 429 || code >= 500; }
        boolean recordError() { return code == 400 || code == 404 || code == 409 || code == 422; }
    }
    static <T> T body(Call<T> call) throws IOException { return read(call.execute(), false); }
    static <T> T optional(Call<T> call) throws IOException { return read(call.execute(), true); }
    private static <T> T read(Response<T> response, boolean optional) throws IOException {
        try {
            if (optional && response.code() == 404) return null;
            if (!response.isSuccessful()) throw new Failure(response.code());
            if (response.body() == null) throw new IOException("Respuesta remota vacía");
            return response.body();
        } finally {
            if (response.errorBody() != null) response.errorBody().close();
        }
    }
}
