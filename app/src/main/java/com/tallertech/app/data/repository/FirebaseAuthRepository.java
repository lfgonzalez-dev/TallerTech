package com.tallertech.app.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

/** The only class that accesses Firebase Auth. The SDK owns persistence and tokens. */
public final class FirebaseAuthRepository {
    public static final class User {
        public final String uid, email, name;
        User(String uid, String email, String name) {
            this.uid = uid; this.email = email; this.name = name;
        }
    }
    public interface Result { void complete(User user, String error); }
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final MutableLiveData<User> session = new MutableLiveData<>();

    private FirebaseAuthRepository() {
        session.postValue(getCurrentUser());
        auth.addAuthStateListener(ignored -> session.setValue(getCurrentUser()));
    }
    private static final class Holder {
        private static final FirebaseAuthRepository INSTANCE = new FirebaseAuthRepository();
    }
    public static FirebaseAuthRepository getInstance() { return Holder.INSTANCE; }
    public LiveData<User> session() { return session; }
    public User getCurrentUser() {
        FirebaseUser user = auth.getCurrentUser();
        return user == null ? null : new User(user.getUid(), user.getEmail(), user.getDisplayName());
    }
    public String getUid() { User user = getCurrentUser(); return user == null ? null : user.uid; }
    public String getEmail() { User user = getCurrentUser(); return user == null ? null : user.email; }
    public boolean isAuthenticated() { return getCurrentUser() != null; }

    public void login(String email, String password, Result result) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) result.complete(getCurrentUser(), null);
            else result.complete(null, message(task.getException()));
        });
    }

    public void validateSession(Result result) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { result.complete(null, null); return; }
        user.reload().addOnCompleteListener(task -> {
            Exception error = task.getException();
            if (!user.getUid().equals(getUid())) {
                result.complete(null, getUid() == null && error instanceof FirebaseAuthInvalidUserException ? message(error) : null);
                return;
            }
            if (!task.isSuccessful() && error instanceof FirebaseAuthInvalidUserException) {
                logout(); result.complete(null, message(error));
            } else {
                // A transient network failure must not discard the SDK's persisted local session.
                result.complete(getCurrentUser(), null);
            }
        });
    }

    public void logout() { auth.signOut(); session.setValue(null); }

    private static String message(Exception error) {
        if (error instanceof FirebaseNetworkException) return FirebaseAuthErrors.message("ERROR_NETWORK_REQUEST_FAILED");
        if (error instanceof FirebaseTooManyRequestsException) return FirebaseAuthErrors.message("ERROR_TOO_MANY_REQUESTS");
        return FirebaseAuthErrors.message(error instanceof FirebaseAuthException
                ? ((FirebaseAuthException) error).getErrorCode() : null);
    }
}
