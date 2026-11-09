package com.tallertech.app.data.remote.dto.auth;
public final class LoginResponse {
    public String token, tokenType;
    public long expiresIn;
    public UserDto user;
}
