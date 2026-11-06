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

public class EditEquipmentFragment extends Fragment {

    private static final String ARG_CLIENT = "client";
    private static final String ARG_TYPE = "type";
    private static final String ARG_BRAND = "brand";
    private static final String ARG_MODEL = "model";
    private static final String ARG_SERIAL = "serial";
    private static final String ARG_DESCRIPTION = "description";

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
            String client,
            String type,
            String brand,
            String model,
            String serial,
            String description) {

        EditEquipmentFragment fragment =
                new EditEquipmentFragment();

        Bundle args = new Bundle();

        args.putString(ARG_CLIENT, client);
        args.putString(ARG_TYPE, type);
        args.putString(ARG_BRAND, brand);
        args.putString(ARG_MODEL, model);
        args.putString(ARG_SERIAL, serial);
        args.putString(ARG_DESCRIPTION, description);

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

        MaterialButton btnSave =
                view.findViewById(R.id.btnSaveEquipment);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelEquipment);

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

        // Precargar valores de los dropdowns sin activar su filtro.
        etClient.setText(
                args.getString(ARG_CLIENT, ""),
                false
        );

        etType.setText(
                args.getString(ARG_TYPE, ""),
                false
        );

        etBrand.setText(
                args.getString(ARG_BRAND, "")
        );

        etModel.setText(
                args.getString(ARG_MODEL, "")
        );

        etSerial.setText(
                args.getString(ARG_SERIAL, "")
        );

        etDescription.setText(
                args.getString(ARG_DESCRIPTION, "")
        );
    }

    private void validarFormulario() {

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

        if (client.isEmpty()) {

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

            // El guardado real se implementará con la capa de datos.
            Toast.makeText(
                    requireContext(),
                    "Datos modificados correctamente",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    private void configurarDropdowns() {

        // Datos ficticios durante el desarrollo del frontend.
        String[] clients = {
                "María López",
                "Carlos Méndez",
                "Ana Torres",
                "Roberto Díaz",
                "Sofía Guzmán"
        };

        String[] equipmentTypes = {
                "Laptop",
                "Smartphone",
                "Tablet",
                "Computadora",
                "Monitor",
                "Impresora",
                "Otro"
        };

        ArrayAdapter<String> clientAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        clients
                );

        ArrayAdapter<String> typeAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        equipmentTypes
                );

        etClient.setAdapter(clientAdapter);
        etType.setAdapter(typeAdapter);
    }
}