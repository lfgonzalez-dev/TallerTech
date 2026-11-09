package com.tallertech.app;

import android.content.Context;
import androidx.lifecycle.*;
import com.tallertech.app.data.repository.AuthRepository;

/** Keeps a single authentication request across Activity recreation. */
public final class LoginViewModel extends ViewModel {
    public static final class State {
        final boolean loading, authenticated;
        final String error;
        State(boolean loading, boolean authenticated, String error) {
            this.loading = loading; this.authenticated = authenticated; this.error = error;
        }
    }
    final MutableLiveData<State> state = new MutableLiveData<>(new State(true, false, null));
    private AuthRepository auth;
    void initialize(Context context) {
        if (auth != null) return;
        auth = new AuthRepository(context.getApplicationContext()); auth.validateSession(result());
    }
    void login(String email, String password) {
        if (state.getValue() != null && state.getValue().loading) return;
        state.setValue(new State(true, false, null)); auth.login(email, password, result());
    }
    private AuthRepository.Result result() {
        return new AuthRepository.Result() {
            @Override public void success() { state.setValue(new State(false, true, null)); }
            @Override public void error(String message) { state.setValue(new State(false, false, message)); }
        };
    }
}
