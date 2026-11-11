package com.tallertech.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.*;
import com.tallertech.app.data.repository.AuthRepository;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private LoginViewModel model;
    private boolean busy;
    @Override protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_TallerTech);
        super.onCreate(savedInstanceState); EdgeToEdge.enable(this); setContentView(R.layout.activity_login);
        tilEmail = findViewById(R.id.tilEmail); tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etMail); etPassword = findViewById(R.id.etPassword);
        etPassword.setSaveEnabled(false);
        btnLogin = findViewById(R.id.btnLogin);
        model = new androidx.lifecycle.ViewModelProvider(this).get(LoginViewModel.class);
        btnLogin.setOnClickListener(v -> validarFormulario());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom); return insets;
        });
        model.state.observe(this, state -> {
            if (!state.loading || state.authenticated) {
                findViewById(R.id.brandSplash).setVisibility(android.view.View.GONE);
            }
            loading(state.loading);
            if (state.authenticated) {
                if (AuthRepository.isOfflineSession()) Toast.makeText(this, "Sin conexión: trabajando con datos locales", Toast.LENGTH_LONG).show();
                result().success();
            } else if (state.error != null) Toast.makeText(this, state.error, Toast.LENGTH_LONG).show();
        });
        model.initialize(getApplicationContext());
    }
    private void validarFormulario() {
        if (busy) return;
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();
        tilEmail.setError(null); tilPassword.setError(null);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.setError("Ingresa un correo electrónico válido"); return; }
        if (password.isEmpty()) { tilPassword.setError("Ingresa tu contraseña"); return; }
        model.login(email, password);
    }
    private AuthRepository.Result result() {
        return new AuthRepository.Result() {
            @Override public void success() {
                if (isFinishing() || isDestroyed()) return;
                etPassword.setText("");
                startActivity(new Intent(LoginActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)); finish();
            }
            @Override public void error(String message) {
                if (isFinishing() || isDestroyed()) return;
                loading(false);
                if (message != null) Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        };
    }
    private void loading(boolean loading) {
        busy = loading; btnLogin.setEnabled(!loading); etEmail.setEnabled(!loading); etPassword.setEnabled(!loading);
        btnLogin.setText(loading ? "Conectando…" : getString(R.string.login_button));
    }
}
