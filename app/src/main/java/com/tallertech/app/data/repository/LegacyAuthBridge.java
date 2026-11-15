package com.tallertech.app.data.repository;

import android.content.Context;
import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.entity.UserEntity;
import com.tallertech.app.data.remote.RetrofitClient;
import com.tallertech.app.data.remote.api.TallerTechApi;
import com.tallertech.app.data.remote.dto.auth.LoginRequest;
import com.tallertech.app.data.remote.dto.auth.LoginResponse;
import com.tallertech.app.data.remote.dto.auth.UserDto;
import com.tallertech.app.data.remote.session.LegacyTokenStore;
import com.tallertech.app.data.remote.session.TokenManager;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import retrofit2.Response;

/** Transitional adapter only. All methods run on the authentication IO executor. */
final class LegacyAuthBridge {
    static final String UNAVAILABLE = "Sesión Firebase activa. No se pudo habilitar el servidor anterior. "
            + "Puedes trabajar con datos locales; para sincronizar, cierra sesión e ingresa con credenciales válidas en ambos sistemas.";
    static final class Session {
        final UserDto user;
        final boolean offline;
        Session(UserDto user, boolean offline) { this.user = user; this.offline = offline; }
    }
    private final TallerTechApi api;
    private final LegacyTokenStore tokens;
    private final Consumer<UserDto> cache;
    private final Supplier<String> currentUid;

    LegacyAuthBridge(Context context) {
        this(RetrofitClient.getApi(context), new TokenManager(context), user -> {
            TallerTechDatabase.getInstance(context).userDao().insert(new UserEntity(
                    user.id, user.name, user.email, user.role, user.updatedAt));
        }, () -> FirebaseAuthRepository.getInstance().getUid());
    }
    LegacyAuthBridge(TallerTechApi api, LegacyTokenStore tokens, Consumer<UserDto> cache, Supplier<String> currentUid) {
        this.api = api; this.tokens = tokens; this.cache = cache; this.currentUid = currentUid;
    }

    Session login(String uid, String email, String password) {
        tokens.clearToken();
        try {
            Response<LoginResponse> response = api.login(new LoginRequest(email, password)).execute();
            try {
                LoginResponse body = response.body();
                if (!response.isSuccessful() || body == null || !"Bearer".equals(body.tokenType)
                        || body.token == null || body.token.trim().isEmpty() || !matches(email, body.user)
                        || !uid.equals(currentUid.get())) return null;
                cache.accept(body.user);
                tokens.saveToken(body.token, uid);
                // Logout/account change may have happened while the network or Keystore was busy.
                if (!uid.equals(currentUid.get())) { tokens.clearTokenIfMatches(body.token); return null; }
                return new Session(body.user, false);
            } finally { if (response.errorBody() != null) response.errorBody().close(); }
        } catch (IOException | RuntimeException error) {
            tokens.clearToken(); return null;
        }
    }

    Session restore(String uid, String email) {
        String token = tokens.getTokenForUser(uid);
        if (token == null) { tokens.clearToken(); return null; }
        try {
            Response<UserDto> response = api.me().execute();
            try {
                if (response.isSuccessful() && matches(email, response.body()) && uid.equals(currentUid.get())
                        && token.equals(tokens.getTokenForUser(uid))) {
                    cache.accept(response.body());
                    return new Session(response.body(), false);
                }
                // Keep a previously validated token on server outages; remote operations still enforce it.
                if (response.code() >= 500) return offline(uid, email, token);
                tokens.clearTokenIfMatches(token); return null;
            } finally { if (response.errorBody() != null) response.errorBody().close(); }
        } catch (IOException error) {
            return offline(uid, email, token);
        } catch (RuntimeException error) {
            tokens.clearTokenIfMatches(token); return null;
        }
    }
    private Session offline(String uid, String email, String token) {
        if (!uid.equals(currentUid.get()) || !token.equals(tokens.getTokenForUser(uid))) return null;
        UserDto user = tokens.offlineUser();
        if (!matches(email, user)) { tokens.clearTokenIfMatches(token); return null; }
        return new Session(user, true);
    }
    static boolean matches(String email, UserDto user) {
        return email != null && user != null && user.id != null && !user.id.isEmpty()
                && user.email != null && email.equalsIgnoreCase(user.email);
    }
    void clear() { tokens.clearToken(); }
}
