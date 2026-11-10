package com.tallertech.app.data.remote.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

/** Only ciphertext is persisted. The AES key stays inside Android Keystore. */
public final class TokenManager {
    private static final String ALIAS = "tallertech_session_aes";
    private final SharedPreferences prefs;
    public TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences("secure_session", Context.MODE_PRIVATE);
    }
    private SecretKey key() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        if (store.containsAlias(ALIAS)) return (SecretKey) store.getKey(ALIAS, null);
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return generator.generateKey();
    }
    public synchronized void saveToken(String token) {
        if (token == null || token.trim().isEmpty()) throw new IllegalArgumentException("Token vacío");
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key());
            String encrypted = Base64.encodeToString(cipher.doFinal(token.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP);
            String iv = Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP);
            if (!prefs.edit().putString("ciphertext", encrypted).putString("iv", iv).commit()) throw new IllegalStateException();
        } catch (Exception ex) { clearToken(); throw new IllegalStateException("No se pudo guardar la sesión de forma segura"); }
    }
    public synchronized String getToken() {
        String encrypted = prefs.getString("ciphertext", null), iv = prefs.getString("iv", null);
        if (encrypted == null || iv == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)));
            return new String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), StandardCharsets.UTF_8);
        } catch (Exception ex) { clearToken(); return null; }
    }
    public synchronized void clearToken() { prefs.edit().clear().commit(); }
    public boolean hasToken() { String token = getToken(); return token != null && !token.trim().isEmpty(); }
    /** Offline local access only, using a token previously received and encrypted by this app.
     * This does not verify a server signature or authorize any remote operation. */
    public com.tallertech.app.data.remote.dto.auth.UserDto offlineUser() {
        String token = getToken();
        if (token == null) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException();
            org.json.JSONObject claims = new org.json.JSONObject(new String(
                    Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP), StandardCharsets.UTF_8));
            if (!"tallertech".equals(claims.getString("iss")) || claims.getLong("exp") <= System.currentTimeMillis() / 1000)
                throw new IllegalArgumentException();
            var user = new com.tallertech.app.data.remote.dto.auth.UserDto();
            user.id = claims.getString("sub"); user.email = claims.getString("email");
            user.name = user.email; user.role = claims.optString("role");
            return user;
        } catch (Exception ex) { clearToken(); return null; }
    }
}
