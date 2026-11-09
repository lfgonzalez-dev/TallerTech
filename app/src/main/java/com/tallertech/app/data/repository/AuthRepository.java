package com.tallertech.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.tallertech.app.data.remote.*;
import com.tallertech.app.data.remote.api.TallerTechApi;
import com.tallertech.app.data.remote.dto.auth.*;
import com.tallertech.app.data.remote.session.TokenManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.*;

/** Authentication only: never writes remote business records into Room. */
public final class AuthRepository {
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static volatile UserDto currentUser;
    private static volatile boolean offlineSession;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final TallerTechApi api;
    private final TokenManager tokens;
    public interface Result { void success(); void error(String message); }
    public AuthRepository(Context context) { api = RetrofitClient.getApi(context); tokens = new TokenManager(context); }
    public static UserDto getCurrentUser() { return currentUser; }
    public static boolean isOfflineSession() { return offlineSession; }
    public void login(String email, String password, Result result) {
        api.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    result.error(response.code() == 401 ? "Correo o contraseña incorrectos" : ApiErrors.message(response)); return;
                }
                LoginResponse body = response.body();
                if (body.user == null || body.token == null || body.token.trim().isEmpty() || !"Bearer".equals(body.tokenType)) {
                    result.error("Respuesta de autenticación inválida"); return;
                }
                IO.execute(() -> {
                    try { tokens.saveToken(body.token); currentUser = body.user; offlineSession = false; main.post(result::success); }
                    catch (IllegalStateException ex) { main.post(() -> result.error(ex.getMessage())); }
                });
            }
            @Override public void onFailure(Call<LoginResponse> call, Throwable error) { result.error(ApiErrors.message(error)); }
        });
    }
    public void validateSession(Result result) {
        IO.execute(() -> {
            if (!tokens.hasToken()) { currentUser = null; main.post(() -> result.error(null)); return; }
            main.post(() -> api.me().enqueue(new Callback<UserDto>() {
                @Override public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body(); offlineSession = false; result.success();
                    } else {
                        currentUser = null;
                        if (response.code() == 401) IO.execute(tokens::clearToken);
                        result.error(ApiErrors.message(response));
                    }
                }
                @Override public void onFailure(Call<UserDto> call, Throwable error) {
                    IO.execute(() -> {
                        UserDto cached = error instanceof java.io.IOException ? tokens.offlineUser() : null;
                        currentUser = cached; offlineSession = cached != null;
                        main.post(() -> {
                            if (cached != null) result.success(); else result.error(ApiErrors.message(error));
                        });
                    });
                }
            }));
        });
    }
    public void logout(Runnable done) {
        IO.execute(() -> { tokens.clearToken(); currentUser = null; offlineSession = false; main.post(done); });
    }
}
