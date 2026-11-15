package com.tallertech.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.tallertech.app.data.remote.dto.auth.UserDto;
import com.tallertech.app.data.sync.SyncManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Firebase owns the app session; Spring Boot credentials are optional and temporary. */
public final class AuthRepository {
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final AtomicInteger generation = new AtomicInteger();
    private static volatile LegacyAuthBridge.Session backend;
    private static volatile String backendUid;
    private static volatile String compatibilityWarning;
    private static volatile String sessionUid;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final FirebaseAuthRepository firebase = FirebaseAuthRepository.getInstance();
    private final LegacyAuthBridge legacy;
    private final Context context;

    public interface Result { void success(); void error(String message); }
    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        legacy = new LegacyAuthBridge(this.context);
    }

    /** Existing 401 callers invalidate only the legacy session, never Firebase. */
    public static void invalidateSession() {
        backend = null; backendUid = null; compatibilityWarning = LegacyAuthBridge.UNAVAILABLE;
    }

    public static UserDto getCurrentUser() {
        FirebaseAuthRepository.User user = FirebaseAuthRepository.getInstance().getCurrentUser();
        if (user == null) return null;
        LegacyAuthBridge.Session session = backend;
        if (user.uid.equals(backendUid) && session != null && LegacyAuthBridge.matches(user.email, session.user))
            return session.user;
        // UI projection only: never insert Firebase UIDs into the backend's Room user table.
        UserDto display = new UserDto();
        display.id = user.uid; display.email = user.email;
        display.name = user.name == null || user.name.trim().isEmpty() ? (user.email == null ? "Usuario" : user.email) : user.name;
        display.role = null;
        return display;
    }
    static void updateCurrentUser(UserDto user) {
        LegacyAuthBridge.Session session = backend;
        FirebaseAuthRepository.User firebaseUser = FirebaseAuthRepository.getInstance().getCurrentUser();
        if (session != null && firebaseUser != null && firebaseUser.uid.equals(backendUid)
                && session.user.id.equals(user.id) && LegacyAuthBridge.matches(firebaseUser.email, user))
            backend = new LegacyAuthBridge.Session(user, session.offline);
    }
    public static boolean isOfflineSession() { LegacyAuthBridge.Session session = backend; return session != null && session.offline; }
    public static String getCompatibilityWarning() { return compatibilityWarning; }
    public static boolean isSessionInitialized() {
        String uid = FirebaseAuthRepository.getInstance().getUid();
        return uid != null && uid.equals(sessionUid);
    }

    public void login(String email, String password, Result result) {
        int request = generation.incrementAndGet();
        invalidateSession();
        IO.execute(legacy::clear);
        firebase.login(email, password, (user, error) -> {
            if (request != generation.get()) return;
            if (user == null) { result.error(error); return; }
            IO.execute(() -> {
                LegacyAuthBridge.Session session = legacy.login(user.uid, user.email, password);
                main.post(() -> finishSession(request, user.uid, session, result));
            });
        });
    }

    public void validateSession(Result result) {
        int request = generation.incrementAndGet();
        firebase.validateSession((user, error) -> {
            if (request != generation.get()) return;
            if (user == null) {
                invalidateSession();
                IO.execute(() -> {
                    legacy.clear();
                    main.post(() -> { if (request == generation.get()) result.error(error); });
                });
                return;
            }
            IO.execute(() -> {
                LegacyAuthBridge.Session session = legacy.restore(user.uid, user.email);
                main.post(() -> finishSession(request, user.uid, session, result));
            });
        });
    }

    private void finishSession(int request, String uid, LegacyAuthBridge.Session session, Result result) {
        if (request != generation.get() || !uid.equals(firebase.getUid())) return;
        sessionUid = uid;
        backendUid = uid; backend = session;
        compatibilityWarning = session == null ? LegacyAuthBridge.UNAVAILABLE : null;
        if (session == null) SyncManager.problem(context, LegacyAuthBridge.UNAVAILABLE);
        else {
            SyncManager.preferences(context).edit().remove("problem").apply();
            SyncManager.request(context);
        }
        result.success();
    }

    public void logout(Runnable done) {
        generation.incrementAndGet();
        firebase.logout();
        backend = null; backendUid = null; compatibilityWarning = null; sessionUid = null;
        androidx.work.WorkManager.getInstance(context).cancelUniqueWork(SyncManager.WORK_NAME);
        IO.execute(() -> { legacy.clear(); main.post(done); });
    }
}
