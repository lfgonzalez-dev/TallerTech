package com.tallertech.app.data.remote.session;

/** Temporary Spring Boot credentials, bound to the Firebase account that obtained them. */
public interface LegacyTokenStore {
    void saveToken(String token, String firebaseUid);
    String getTokenForUser(String firebaseUid);
    void clearToken();
    void clearTokenIfMatches(String token);
    com.tallertech.app.data.remote.dto.auth.UserDto offlineUser();
}
