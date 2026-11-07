package com.tallertech.app.ui.equipment;

import android.os.Bundle;
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

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.repository.ClientRepository;
import com.tallertech.app.data.repository.EquipmentRepository;
import com.tallertech.app.data.repository.ResultCallback;
import java.util.ArrayList;
import java.util.List;

public class EditEquipmentFragment extends Fragment {
    private EquipmentEntity equipment;
    private ClientEntity selectedClient;
    private MaterialButton btnSave;
    private boolean saving;

    private static class ClientOption {
        final ClientEntity client;
        ClientOption(ClientEntity client) { this.client = client; }
        @NonNull @Override public String toString() { return client.getName() + " · " + client.getPhone(); }
    }

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

    public EditEquipmentFragment() {

        // Reutilizamos el formulario de registro.
        super(R.layout.fragment_register_equipment);
    }

    public static EditEquipmentFragment newInstance(
            String id) {

        EditEquipmentFragment fragment =
                new EditEquipmentFragment();

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

        // Elementos del formulario.
        MaterialToolbar toolbar =
                view.findViewById(R.id.toolbarRegisterEquipment);

        tilClient =
                view.findViewById(R.id.tilEquipmentClient);

        tilType =
                view.findViewById(R.id.tilEquipmentType);

        tilBrand =
                view.findViewById(R.id.tilEquipmentBrand);

        tilModel =
                view.findViewById(R.id.tilEquipmentModel);

        tilSerial =
                view.findViewById(R.id.tilEquipmentSerial);

        etClient =
                view.findViewById(R.id.etEquipmentClient);

        etType =
                view.findViewById(R.id.etEquipmentType);

        etBrand =
                view.findViewById(R.id.etEquipmentBrand);

        etModel =
                view.findViewById(R.id.etEquipmentModel);

        etSerial =
                view.findViewById(R.id.etEquipmentSerial);

        etDescription =
                view.findViewById(R.id.etEquipmentDescription);

        btnSave =
                view.findViewById(R.id.btnSaveEquipment);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelEquipment);

        equipment = null;
        selectedClient = null;
        saving = false;
        btnSave.setEnabled(false);
        configurarDropdowns();

        // Adaptamos el formulario al modo edición.
        toolbar.setTitle("Editar equipo");
        toolbar.setSubtitle("Actualiza la información del dispositivo");

        btnSave.setText("Guardar cambios");

        // Precargar información actual.
        cargarDatosEquipo();

        btnSave.setOnClickListener(v ->
                validarFormulario()
        );

        // Regresar sin realizar cambios.
        btnCancel.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
    }

    private void cargarDatosEquipo() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        View currentView = getView();
        etClient.setEnabled(false);
        new EquipmentRepository(requireContext()).getById(args.getString("id"), new ResultCallback<EquipmentEntity>() {
            @Override public void onResult(EquipmentEntity result) {
                if (!isAdded() || getView() != currentView) return;
                equipment = result;
                etType.setText(result.getType(), false);
                etBrand.setText(result.getBrand());
                etModel.setText(result.getModel());
                etSerial.setText(result.getSerial());
                etDescription.setText(result.getDescription());
                new ClientRepository(requireContext()).getAll(new ClientRepository.ClientsCallback() {
                    @Override public void onResult(List<ClientEntity> clients) {
                        if (!isAdded() || getView() != currentView) return;
                        List<ClientOption> options = new ArrayList<>();
                        for (ClientEntity client : clients) {
                            options.add(new ClientOption(client));
                            if (client.getId().equals(equipment.getClientId())) selectedClient = client;
                        }
                        etClient.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, options));
                        if (selectedClient != null) etClient.setText(new ClientOption(selectedClient).toString(), false);
                        etClient.setOnItemClickListener((parent, v, position, id) -> {
                            selectedClient = ((ClientOption) parent.getItemAtPosition(position)).client;
                            tilClient.setError(null);
                        });
                        etClient.setEnabled(true);
                        btnSave.setEnabled(selectedClient != null);
                    }
                    @Override public void onError(Exception e) { mostrarError(currentView); }
                });
            }
            @Override public void onError(Exception e) { mostrarError(currentView); }
        });
    }

    private void validarFormulario() {
        if (equipment == null || saving) return;

        String client =
                etClient.getText().toString().trim();

        String type =
                etType.getText().toString().trim();

        String brand =
                etBrand.getText().toString().trim();

        String model =
                etModel.getText().toString().trim();

        String serial =
                etSerial.getText().toString().trim();

        // Limpiar errores anteriores.
        tilClient.setError(null);
        tilType.setError(null);
        tilBrand.setError(null);
        tilModel.setError(null);
        tilSerial.setError(null);

        boolean formularioValido = true;

        if (selectedClient == null || !client.equals(new ClientOption(selectedClient).toString())) {

            tilClient.setError(
                    "Ingresa o selecciona un cliente"
            );

            formularioValido = false;
        }

        if (type.isEmpty()) {

            tilType.setError(
                    "Ingresa el tipo de equipo"
            );

            formularioValido = false;
        }

        if (brand.isEmpty()) {

            tilBrand.setError(
                    "Ingresa la marca"
            );

            formularioValido = false;
        }

        if (model.isEmpty()) {

            tilModel.setError(
                    "Ingresa el modelo"
            );

            formularioValido = false;
        }

        if (serial.isEmpty()) {

            tilSerial.setError(
                    "Ingresa el número de serie"
            );

            formularioValido = false;
        }

        if (formularioValido) {

            equipment.setClientId(selectedClient.getId());
            equipment.setType(type);
            equipment.setBrand(brand);
            equipment.setModel(model);
            equipment.setSerial(serial);
            equipment.setDescription(etDescription.getText().toString().trim());
            saving = true;
            btnSave.setEnabled(false);
            View currentView = getView();
            new EquipmentRepository(requireContext()).update(equipment, new ResultCallback<Void>() {
                @Override public void onResult(Void result) {
                    if (!isAdded() || getView() != currentView) return;
                    Toast.makeText(requireContext(), "Datos modificados correctamente", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                }
                @Override public void onError(Exception e) {
                    if (!isAdded() || getView() != currentView) return;
                    saving = false;
                    btnSave.setEnabled(true);
                    Toast.makeText(requireContext(), "Error al guardar: verifica la serie única y el cliente", Toast.LENGTH_LONG).show();
                }
            });
        }
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

        etType.setAdapter(typeAdapter);
    }

    private void mostrarError(View view) {
        if (!isAdded() || getView() != view) return;
        Toast.makeText(requireContext(), "Error al cargar equipo o clientes", Toast.LENGTH_SHORT).show();
    }
}
