package com.tallertech.app.data.remote.interceptor;
import com.tallertech.app.data.remote.session.TokenManager;
import com.tallertech.app.data.repository.FirebaseAuthRepository;
import java.io.IOException;
import okhttp3.*;

public final class AuthInterceptor implements Interceptor {
    private final TokenManager tokens;
    public AuthInterceptor(TokenManager tokens) { this.tokens = tokens; }
    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        boolean login = request.url().encodedPath().equals("/api/auth/login");
        String token = null;
        if (!login) {
            token = tokens.getTokenForUser(FirebaseAuthRepository.getInstance().getUid());
            if (token == null) throw new IOException("Inicia sesión nuevamente para habilitar el servidor anterior");
            request = request.newBuilder().header("Authorization", "Bearer " + token).build();
        }
        Response response = chain.proceed(request);
        if (response.code() == 401 && !login) tokens.clearTokenIfMatches(token);
        return response;
    }
}
