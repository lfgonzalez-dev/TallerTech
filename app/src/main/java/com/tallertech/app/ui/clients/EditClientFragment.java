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
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.repository.ClientRepository;
import com.tallertech.app.data.repository.ResultCallback;

public class EditClientFragment extends Fragment {
    private ClientEntity client;
    private MaterialButton btnSave;
    private boolean saving;

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
            String id) {

        EditClientFragment fragment = new EditClientFragment();

        Bundle args = new Bundle();

        args.putString("id", id);

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

        btnSave =
                view.findViewById(R.id.btnSaveClient);

        // Cambiamos el formulario al modo edición.
        toolbar.setTitle("Editar cliente");
        toolbar.setSubtitle("Actualiza la información del cliente");

        btnSave.setText("Guardar cambios");

        // Cargar los datos actuales del cliente.
        client = null;
        saving = false;
        btnSave.setEnabled(false);
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

        View currentView = getView();
        new ClientRepository(requireContext()).getById(args.getString("id"), new ResultCallback<ClientEntity>() {
            @Override public void onResult(ClientEntity result) {
                if (!isAdded() || getView() != currentView) return;
                client = result;
                etName.setText(client.getName());
                etPhone.setText(client.getPhone());
                etEmail.setText(client.getEmail());
                etAddress.setText(client.getAddress());
                etNotes.setText(client.getNotes());
                btnSave.setEnabled(true);
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != currentView) return;
                Toast.makeText(requireContext(), "Error al cargar cliente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarFormulario() {
        if (client == null || saving) return;

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

            client.setName(name);
            client.setPhone(phone);
            client.setEmail(email);
            client.setAddress(etAddress.getText().toString().trim());
            client.setNotes(etNotes.getText().toString().trim());
            saving = true;
            btnSave.setEnabled(false);
            View currentView = getView();
            new ClientRepository(requireContext()).update(client, new ResultCallback<Void>() {
                @Override public void onResult(Void result) {
                    if (!isAdded() || getView() != currentView) return;
                    Toast.makeText(requireContext(), "Datos modificados correctamente", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                }
                @Override public void onError(Exception e) {
                    if (!isAdded() || getView() != currentView) return;
                    saving = false;
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Error al actualizar cliente", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
