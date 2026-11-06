package com.tallertech.app.ui.equipment;

import android.os.Bundle;
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

        MaterialButton btnSave =
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

        // Datos temporales hasta conectar la capa de datos.
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

    private void validarFormulario() {

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

        if (client.isEmpty()) {
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

            // Por ahora simulamos el registro.
            Toast.makeText(
                    requireContext(),
                    "Datos del equipo válidos",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}