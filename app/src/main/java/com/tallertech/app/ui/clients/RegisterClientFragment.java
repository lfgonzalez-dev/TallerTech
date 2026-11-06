package com.tallertech.app.ui.clients;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tallertech.app.R;
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.repository.ClientRepository;

import java.util.UUID;

public class RegisterClientFragment extends Fragment {

    private TextInputLayout tilName;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;

    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private TextInputEditText etAddress;
    private TextInputEditText etNotes;

    private ClientRepository clientRepository;

    public RegisterClientFragment() {
        super(R.layout.fragment_register_client);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Repository encargado de Room.
        clientRepository =
                new ClientRepository(requireContext());

        tilName =
                view.findViewById(R.id.tilClientName);

        tilPhone =
                view.findViewById(R.id.tilClientPhone);

        tilEmail =
                view.findViewById(R.id.tilClientEmail);

        etName =
                view.findViewById(R.id.etClientName);

        etPhone =
                view.findViewById(R.id.etClientPhone);

        etEmail =
                view.findViewById(R.id.etClientEmail);

        etAddress =
                view.findViewById(R.id.etClientAddress);

        etNotes =
                view.findViewById(R.id.etClientNotes);

        MaterialButton btnSave =
                view.findViewById(R.id.btnSaveClient);

        btnSave.setOnClickListener(v ->
                validarFormulario()
        );
    }

    private void validarFormulario() {

        String name =
                etName.getText().toString().trim();

        String phone =
                etPhone.getText().toString().trim();

        String email =
                etEmail.getText().toString().trim();

        String address =
                etAddress.getText().toString().trim();

        String notes =
                etNotes.getText().toString().trim();

        // Limpiar errores anteriores.
        tilName.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);

        boolean formularioValido = true;

        if (name.isEmpty()) {

            tilName.setError(
                    "Ingresa el nombre del cliente"
            );

            formularioValido = false;
        }

        if (phone.isEmpty()) {

            tilPhone.setError(
                    "Ingresa el teléfono del cliente"
            );

            formularioValido = false;
        }

        if (!email.isEmpty()
                && !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            tilEmail.setError(
                    "Ingresa un correo electrónico válido"
            );

            formularioValido = false;
        }

        if (formularioValido) {

            guardarCliente(
                    name,
                    phone,
                    email,
                    address,
                    notes
            );
        }
    }

    private void guardarCliente(
            String name,
            String phone,
            String email,
            String address,
            String notes) {

        // UUID permite crear registros incluso sin conexión.
        String id =
                UUID.randomUUID().toString();

        long now =
                System.currentTimeMillis();

        ClientEntity client =
                new ClientEntity(
                        id,
                        name,
                        phone,
                        email,
                        address,
                        notes,
                        now,
                        "PENDING"
                );

        clientRepository.insert(
                client,
                new ClientRepository.OperationCallback() {

                    @Override
                    public void onSuccess() {

                        Toast.makeText(
                                requireContext(),
                                "Cliente registrado correctamente",
                                Toast.LENGTH_SHORT
                        ).show();

                        // Regresar a Clientes.
                        requireActivity()
                                .getSupportFragmentManager()
                                .popBackStack();
                    }

                    @Override
                    public void onError(Exception e) {

                        Toast.makeText(
                                requireContext(),
                                "Error al guardar el cliente",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}