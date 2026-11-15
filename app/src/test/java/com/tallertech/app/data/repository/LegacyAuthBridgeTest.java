package com.tallertech.app.data.repository;

import com.tallertech.app.data.remote.api.TallerTechApi;
import com.tallertech.app.data.remote.dto.auth.LoginResponse;
import com.tallertech.app.data.remote.dto.auth.UserDto;
import com.tallertech.app.data.remote.session.LegacyTokenStore;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static org.junit.Assert.*;

public class LegacyAuthBridgeTest {
    private final AtomicReference<String> uid = new AtomicReference<>("firebase-a");
    private final Store store = new Store();
    private Response<?> response;
    private IOException failure;
    private Runnable duringRequest = () -> {};
    private UserDto cached;
    private LegacyAuthBridge bridge;

    @Before public void setUp() {
        TallerTechApi api = (TallerTechApi) Proxy.newProxyInstance(TallerTechApi.class.getClassLoader(),
                new Class<?>[]{TallerTechApi.class}, (proxy, method, args) -> new FakeCall());
        bridge = new LegacyAuthBridge(api, store, user -> cached = user, uid::get);
    }
    private static UserDto user(String email) {
        UserDto user = new UserDto(); user.id = "backend-uuid"; user.email = email;
        user.name = "Técnico"; user.role = "TECHNICIAN"; return user;
    }
    private void successfulLogin(String email) {
        LoginResponse body = new LoginResponse(); body.token = "jwt"; body.tokenType = "Bearer";
        body.user = user(email); response = Response.success(body);
    }
    @Test public void matchingBackendLoginBindsJwtToFirebaseUid() {
        successfulLogin("USER@example.com");
        LegacyAuthBridge.Session session = bridge.login(uid.get(), "user@example.com", "password");
        assertNotNull(session); assertFalse(session.offline);
        assertEquals("jwt", store.getTokenForUser(uid.get())); assertSame(session.user, cached);
        assertNull(store.getTokenForUser("firebase-b"));
    }
    @Test public void backendRejectionLeavesNoLegacySession() {
        store.saveToken("old", uid.get());
        response = Response.error(401, ResponseBody.Companion.create("", null));
        assertNull(bridge.login(uid.get(), "user@example.com", "password"));
        assertNull(store.token); assertNull(cached);
    }
    @Test public void backendNetworkFailureLeavesNoLegacySession() {
        failure = new IOException("network");
        assertNull(bridge.login(uid.get(), "user@example.com", "password")); assertNull(store.token);
    }
    @Test public void differentBackendAccountCannotBeAttached() {
        successfulLogin("other@example.com");
        assertNull(bridge.login(uid.get(), "user@example.com", "password"));
        assertNull(store.token); assertNull(cached);
    }
    @Test public void logoutWhileLoginRequestRunsDoesNotPersistJwt() {
        successfulLogin("user@example.com"); duringRequest = () -> uid.set(null);
        assertNull(bridge.login("firebase-a", "user@example.com", "password")); assertNull(store.token);
    }
    @Test public void accountSwitchWhileLoginRequestRunsDoesNotPersistJwt() {
        successfulLogin("user@example.com"); duringRequest = () -> uid.set("firebase-b");
        assertNull(bridge.login("firebase-a", "user@example.com", "password")); assertNull(store.token);
    }
    @Test public void logoutWhileCachingDoesNotLeaveJwt() {
        successfulLogin("user@example.com");
        TallerTechApi api = (TallerTechApi) Proxy.newProxyInstance(TallerTechApi.class.getClassLoader(),
                new Class<?>[]{TallerTechApi.class}, (proxy, method, args) -> new FakeCall());
        bridge = new LegacyAuthBridge(api, store, user -> uid.set(null), uid::get);
        assertNull(bridge.login("firebase-a", "user@example.com", "password")); assertNull(store.token);
    }
    @Test public void restoreUsesOnlyTokenBoundToActiveUid() {
        store.saveToken("jwt", "firebase-b"); response = Response.success(user("user@example.com"));
        assertNull(bridge.restore(uid.get(), "user@example.com")); assertNull(store.token);
    }
    @Test public void restoreSuccessfulBackendSession() {
        store.saveToken("jwt", uid.get()); response = Response.success(user("user@example.com"));
        assertNotNull(bridge.restore(uid.get(), "user@example.com")); assertNotNull(cached);
    }
    @Test public void unauthorizedRestoreClearsOnlyLegacyToken() {
        store.saveToken("jwt", uid.get()); response = Response.error(401, ResponseBody.Companion.create("", null));
        assertNull(bridge.restore(uid.get(), "user@example.com")); assertNull(store.token);
        assertEquals("firebase-a", uid.get());
    }
    @Test public void networkRestoreCanUseMatchingPreviouslyIssuedJwt() {
        store.saveToken("jwt", uid.get()); store.offline = user("user@example.com"); failure = new IOException("offline");
        LegacyAuthBridge.Session session = bridge.restore(uid.get(), "user@example.com");
        assertNotNull(session); assertTrue(session.offline); assertEquals("jwt", store.token);
    }
    @Test public void offlineIdentityMismatchCannotRestoreBackendProfile() {
        store.saveToken("jwt", uid.get()); store.offline = user("other@example.com"); failure = new IOException("offline");
        assertNull(bridge.restore(uid.get(), "user@example.com"));
        assertNull(store.token);
    }
    @Test public void missingBackendIdCannotBeAccepted() {
        UserDto user = user("user@example.com"); user.id = null;
        assertFalse(LegacyAuthBridge.matches("user@example.com", user));
    }

    private static class Store implements LegacyTokenStore {
        String token, owner;
        UserDto offline;
        public void saveToken(String token, String owner) { this.token = token; this.owner = owner; }
        public String getTokenForUser(String uid) { return uid != null && uid.equals(owner) ? token : null; }
        public void clearToken() { token = null; owner = null; }
        public void clearTokenIfMatches(String expected) { if (expected != null && expected.equals(token)) clearToken(); }
        public UserDto offlineUser() { return offline; }
    }
    private class FakeCall implements Call<Object> {
        @SuppressWarnings("unchecked") public Response<Object> execute() throws IOException {
            duringRequest.run();
            if (failure != null) throw failure;
            return (Response<Object>) response;
        }
        public void enqueue(Callback<Object> callback) { throw new UnsupportedOperationException(); }
        public boolean isExecuted() { return false; }
        public void cancel() {}
        public boolean isCanceled() { return false; }
        public Call<Object> clone() { return new FakeCall(); }
        public Request request() { return new Request.Builder().url("https://example.invalid/").build(); }
        public Timeout timeout() { return Timeout.NONE; }
    }
}
