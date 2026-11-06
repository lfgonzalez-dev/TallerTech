package com.tallertech.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.util.Patterns;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private MaterialButton btnLogin;

    private void validarFormulario() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        //LImpiamos errores anteriores
        tilEmail.setError(null);
        tilPassword.setError(null);

        boolean formularioValido = true;

        //Validar correo vacío
        if (email.isEmpty()) {
            tilEmail.setError("Ingresa tu correo electrónico");
            formularioValido = false;
        //Validar formato de correo
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        tilEmail.setError("Ingresa un correo electrónico valido");
        formularioValido = false;
        }
        //Validar contraseña vacia
        if (password.isEmpty()) {
            tilPassword.setError("Ingresa tu contraseña");
            formularioValido = false;
        }

        if (formularioValido) {
            abrirMainActivity();
        }
    }

    private void abrirMainActivity() {
        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        etEmail = findViewById(R.id.etMail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            validarFormulario();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}