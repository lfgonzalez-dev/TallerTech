package com.tallertech.app.ui.equipment;

import android.os.Bundle;
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.repository.ClientRepository;
import com.tallertech.app.data.repository.EquipmentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tallertech.app.R;

public class RegisterEquipmentFragment extends Fragment {

    private ClientEntity selectedClient;
    private MaterialButton btnSave;
    private boolean saving;

    private TextInputLayout tilClient;
    private TextInputLayout tilType;
    private TextInputLayout tilBrand;
    private TextInputLayout tilModel;
    private TextInputLayout tilSerial;

    private AutoCompleteTextView etClient;
    private AutoCompleteTextView etType;

    private TextInputEditText etBrand;
    private TextInputEditText etModel;
    private TextInputEditText etSerial;
    private TextInputEditText etDescription;

    public RegisterEquipmentFragment() {
        super(R.layout.fragment_register_equipment);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Referencias a los campos del formulario.
        tilClient = view.findViewById(R.id.tilEquipmentClient);
        tilType = view.findViewById(R.id.tilEquipmentType);
        tilBrand = view.findViewById(R.id.tilEquipmentBrand);
        tilModel = view.findViewById(R.id.tilEquipmentModel);
        tilSerial = view.findViewById(R.id.tilEquipmentSerial);

        etClient = view.findViewById(R.id.etEquipmentClient);
        etType = view.findViewById(R.id.etEquipmentType);

        etBrand = view.findViewById(R.id.etEquipmentBrand);
        etModel = view.findViewById(R.id.etEquipmentModel);
        etSerial = view.findViewById(R.id.etEquipmentSerial);
        etDescription = view.findViewById(R.id.etEquipmentDescription);

        selectedClient = null;
        saving = false;
        btnSave =
                view.findViewById(R.id.btnSaveEquipment);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelEquipment);

        // Configurar los selectores de cliente y tipo.
        configurarDropdowns();

        btnSave.setOnClickListener(v ->
                validarFormulario()
        );

        btnCancel.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
    }

    private void configurarDropdowns() {

        String[] equipmentTypes = {
                "Laptop",
                "Smartphone",
                "Tablet",
                "Computadora",
                "Monitor",
                "Impresora",
                "Otro"
        };

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        equipmentTypes
                );

        cargarClientes();
        etType.setAdapter(typeAdapter);
    }

    private static class ClientOption {
        final ClientEntity client;

        ClientOption(ClientEntity client) {
            this.client = client;
        }

        @NonNull
        @Override
        public String toString() {
            return client.getName();
        }
    }

    private void cargarClientes() {
        View currentView = getView();
        etClient.setEnabled(false);
        new ClientRepository(requireContext()).getAll(new ClientRepository.ClientsCallback() {
            @Override
            public void onResult(List<ClientEntity> clients) {
                if (!isAdded() || getView() != currentView || currentView == null) return;
                List<ClientOption> names = new ArrayList<>();
                for (ClientEntity client : clients) names.add(new ClientOption(client));
                ArrayAdapter<ClientOption> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, names);
                etClient.setAdapter(adapter);
                etClient.setOnItemClickListener((parent, view, position, id) -> {
                    selectedClient = ((ClientOption) parent.getItemAtPosition(position)).client;
                    etClient.setText(selectedClient.getName(), false);
                    tilClient.setError(null);
                });
                etClient.setEnabled(!clients.isEmpty());
                if (clients.isEmpty()) tilClient.setError("Registra un cliente antes de agregar equipos");
            }
            @Override
            public void onError(Exception e) {
                if (!isAdded() || getView() != currentView || currentView == null) return;
                tilClient.setError("Error al cargar clientes");
            }
        });
    }

    private void validarFormulario() {

        if (saving) return;
        String client = etClient.getText().toString().trim();
        String type = etType.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String serial = etSerial.getText().toString().trim();

        // Limpiar errores anteriores.
        tilClient.setError(null);
        tilType.setError(null);
        tilBrand.setError(null);
        tilModel.setError(null);
        tilSerial.setError(null);

        boolean formularioValido = true;

        if (selectedClient == null || !client.equals(selectedClient.getName())) {
            tilClient.setError("Selecciona un cliente");
            formularioValido = false;
        }

        if (type.isEmpty()) {
            tilType.setError("Selecciona el tipo de equipo");
            formularioValido = false;
        }

        if (brand.isEmpty()) {
            tilBrand.setError("Ingresa la marca");
            formularioValido = false;
        }

        if (model.isEmpty()) {
            tilModel.setError("Ingresa el modelo");
            formularioValido = false;
        }

        if (serial.isEmpty()) {
            tilSerial.setError("Ingresa el número de serie");
            formularioValido = false;
        }

        if (formularioValido) {

            guardarEquipo(type, brand, model, serial);
        }
    }

    private void guardarEquipo(String type, String brand, String model, String serial) {
        EquipmentEntity equipment = new EquipmentEntity(UUID.randomUUID().toString(),
                selectedClient.getId(), type, brand, model, serial,
                etDescription.getText().toString().trim(), System.currentTimeMillis(), "PENDING");
        saving = true;
        btnSave.setEnabled(false);
        View currentView = getView();
        new EquipmentRepository(requireContext()).insert(equipment,
                new EquipmentRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded() || getView() != currentView || currentView == null) return;
                        Toast.makeText(requireContext(), "Equipo registrado correctamente", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                    @Override
                    public void onError(Exception e) {
                        if (!isAdded() || getView() != currentView || currentView == null) return;
                        saving = false;
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(), "Error al guardar: verifica la serie única y el cliente",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
