package com.tallertech.app.ui.clients;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tallertech.app.R;

public class EditClientFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_ADDRESS = "address";
    private static final String ARG_NOTES = "notes";

    private TextInputLayout tilName;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;

    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private TextInputEditText etAddress;
    private TextInputEditText etNotes;

    public EditClientFragment() {

        // Reutilizamos el mismo formulario usado para registrar clientes.
        super(R.layout.fragment_register_client);
    }

    public static EditClientFragment newInstance(
            String name,
            String phone,
            String email,
            String address,
            String notes) {

        EditClientFragment fragment = new EditClientFragment();

        Bundle args = new Bundle();

        args.putString(ARG_NAME, name);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_NOTES, notes);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Referencias a los componentes del formulario.
        MaterialToolbar toolbar =
                view.findViewById(R.id.toolbarRegisterClient);

        tilName = view.findViewById(R.id.tilClientName);
        tilPhone = view.findViewById(R.id.tilClientPhone);
        tilEmail = view.findViewById(R.id.tilClientEmail);

        etName = view.findViewById(R.id.etClientName);
        etPhone = view.findViewById(R.id.etClientPhone);
        etEmail = view.findViewById(R.id.etClientEmail);
        etAddress = view.findViewById(R.id.etClientAddress);
        etNotes = view.findViewById(R.id.etClientNotes);

        MaterialButton btnSave =
                view.findViewById(R.id.btnSaveClient);

        // Cambiamos el formulario al modo edición.
        toolbar.setTitle("Editar cliente");
        toolbar.setSubtitle("Actualiza la información del cliente");

        btnSave.setText("Guardar cambios");

        // Cargar los datos actuales del cliente.
        cargarDatosCliente();

        // Validar datos al presionar guardar.
        btnSave.setOnClickListener(v ->
                validarFormulario()
        );
    }

    private void cargarDatosCliente() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        etName.setText(args.getString(ARG_NAME, ""));
        etPhone.setText(args.getString(ARG_PHONE, ""));
        etEmail.setText(args.getString(ARG_EMAIL, ""));
        etAddress.setText(args.getString(ARG_ADDRESS, ""));
        etNotes.setText(args.getString(ARG_NOTES, ""));
    }

    private void validarFormulario() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // Limpiar errores anteriores.
        tilName.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);

        boolean formularioValido = true;

        if (name.isEmpty()) {

            tilName.setError("Ingresa el nombre del cliente");
            formularioValido = false;
        }

        if (phone.isEmpty()) {

            tilPhone.setError("Ingresa el teléfono del cliente");
            formularioValido = false;
        }

        if (!email.isEmpty()
                && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            tilEmail.setError("Ingresa un correo electrónico válido");
            formularioValido = false;
        }

        if (formularioValido) {

            // Por ahora simulamos la actualización.
            Toast.makeText(
                    requireContext(),
                    "Datos modificados correctamente",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}