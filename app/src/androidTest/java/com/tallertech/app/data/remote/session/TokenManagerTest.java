package com.tallertech.app.data.remote.session;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Uses real Keystore encryption with isolated preferences; never changes the app's session. */
@RunWith(AndroidJUnit4.class)
public class TokenManagerTest {
    private Context context;
    private String preferenceName;
    private TokenManager tokens;

    @Before public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        preferenceName = "test_session_" + UUID.randomUUID();
        Context isolated = new ContextWrapper(context) {
            @Override public Context getApplicationContext() { return this; }
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return context.getSharedPreferences(preferenceName, mode);
            }
        };
        tokens = new TokenManager(isolated);
    }
    @After public void tearDown() { context.deleteSharedPreferences(preferenceName); }

    @Test public void encryptedTokenIsVisibleOnlyToItsOwner() {
        tokens.saveToken("private-jwt", "firebase-a");
        assertEquals("private-jwt", tokens.getTokenForUser("firebase-a"));
        assertNull(tokens.getTokenForUser("firebase-b"));
        assertNull(tokens.getTokenForUser(null));
        assertFalse(context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
                .getAll().containsValue("private-jwt"));
    }
    @Test public void delayedUnauthorizedResponseCannotClearNewToken() {
        tokens.saveToken("old", "firebase-a");
        tokens.saveToken("new", "firebase-a");
        tokens.clearTokenIfMatches("old");
        assertEquals("new", tokens.getTokenForUser("firebase-a"));
        tokens.clearTokenIfMatches("new");
        assertNull(tokens.getTokenForUser("firebase-a"));
    }
    @Test public void logoutClearsTokenAndOwner() {
        tokens.saveToken("jwt", "firebase-a"); tokens.clearToken();
        assertNull(tokens.getTokenForUser("firebase-a"));
        assertTrue(context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE).getAll().isEmpty());
    }
}
