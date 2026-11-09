package com.tallertech.app.data.remote.dto.auth;
public final class LoginRequest {
    public final String email, password;
    public LoginRequest(String email, String password) { this.email = email; this.password = password; }
}
