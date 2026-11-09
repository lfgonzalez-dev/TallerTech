package com.tallertech.app.data.remote.interceptor;
import com.tallertech.app.data.remote.session.TokenManager;
import java.io.IOException;
import okhttp3.*;

public final class AuthInterceptor implements Interceptor {
    private final TokenManager tokens;
    public AuthInterceptor(TokenManager tokens) { this.tokens = tokens; }
    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        boolean login = request.url().encodedPath().equals("/api/auth/login");
        if (!login) {
            String token = tokens.getToken();
            if (token != null && !token.trim().isEmpty()) request = request.newBuilder().header("Authorization", "Bearer " + token).build();
        }
        Response response = chain.proceed(request);
        if (response.code() == 401 && !login) tokens.clearToken();
        return response;
    }
}
