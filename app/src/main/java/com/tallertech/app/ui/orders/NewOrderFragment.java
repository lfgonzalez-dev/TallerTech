package com.tallertech.app.ui.orders;

import android.app.DatePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewOrderFragment extends Fragment {

    private TextInputLayout tilClient;
    private TextInputLayout tilEquipment;
    private TextInputLayout tilIssue;

    private AutoCompleteTextView etClient;
    private AutoCompleteTextView etEquipment;
    private AutoCompleteTextView etTechnician;

    private TextInputEditText etIssue;
    private TextInputEditText etDate;

    private final Calendar selectedDate =
            Calendar.getInstance();

    private final Map<String, String[]> equipmentByClient =
            new HashMap<>();

    public NewOrderFragment() {
        super(R.layout.fragment_new_order);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Referencias de los campos.
        tilClient =
                view.findViewById(R.id.tilOrderClient);

        tilEquipment =
                view.findViewById(R.id.tilOrderEquipment);

        tilIssue =
                view.findViewById(R.id.tilOrderIssue);

        etClient =
                view.findViewById(R.id.etOrderClient);

        etEquipment =
                view.findViewById(R.id.etOrderEquipment);

        etTechnician =
                view.findViewById(R.id.etOrderTechnician);

        etIssue =
                view.findViewById(R.id.etOrderIssue);

        etDate =
                view.findViewById(R.id.etOrderDate);

        MaterialButton btnCreate =
                view.findViewById(R.id.btnCreateOrder);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelOrder);

        configurarDatosPrueba();
        configurarClientes();
        configurarTecnicos();
        configurarFecha();

        // Cambiar equipos cuando se selecciona otro cliente.
        etClient.setOnItemClickListener(
                (parent, selectedView, position, id) -> {

                    String client =
                            parent.getItemAtPosition(position)
                                    .toString();

                    cargarEquiposCliente(client);

                    tilClient.setError(null);
                }
        );

        etEquipment.setOnItemClickListener(
                (parent, selectedView, position, id) ->
                        tilEquipment.setError(null)
        );

        btnCreate.setOnClickListener(v ->
                validarFormulario()
        );

        // Regresar sin crear la orden.
        btnCancel.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
    }

    private void configurarDatosPrueba() {

        // Relación temporal cliente -> equipos.
        equipmentByClient.put(
                "María López",
                new String[]{
                        "Dell Inspiron 15 · DL-98541",
                        "Samsung Galaxy A54 · SM-A546"
                }
        );

        equipmentByClient.put(
                "Carlos Méndez",
                new String[]{
                        "Samsung Galaxy A54 · SM-A546"
                }
        );

        equipmentByClient.put(
                "Ana Torres",
                new String[]{
                        "Lenovo IdeaPad 3 · LN-45681"
                }
        );

        equipmentByClient.put(
                "Roberto Díaz",
                new String[]{
                        "HP 240 G8 · HP-24853",
                        "Samsung Galaxy Tab S8 · TB-10294"
                }
        );

        equipmentByClient.put(
                "Sofía Guzmán",
                new String[]{
                        "HP ProDesk 400 · HP-78421"
                }
        );
    }

    private void configurarClientes() {

        String[] clients = {
                "María López",
                "Carlos Méndez",
                "Ana Torres",
                "Roberto Díaz",
                "Sofía Guzmán"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        clients
                );

        etClient.setAdapter(adapter);
    }

    private void configurarTecnicos() {

        String[] technicians = {
                "Sin asignar",
                "Carlos Reyes",
                "Andrea Martínez"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        technicians
                );

        etTechnician.setAdapter(adapter);

        // Valor inicial.
        etTechnician.setText(
                "Sin asignar",
                false
        );
    }

    private void cargarEquiposCliente(String client) {

        String[] equipment =
                equipmentByClient.get(client);

        if (equipment == null) {
            equipment = new String[0];
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        equipment
                );

        etEquipment.setAdapter(adapter);

        // Limpiar el equipo anterior al cambiar de cliente.
        etEquipment.setText("", false);
    }

    private void configurarFecha() {

        // Mostrar la fecha actual al abrir el formulario.
        actualizarFecha();

        etDate.setOnClickListener(v ->
                mostrarSelectorFecha()
        );
    }

    private void mostrarSelectorFecha() {

        DatePickerDialog dialog =
                new DatePickerDialog(
                        requireContext(),
                        (view, year, month, dayOfMonth) -> {

                            selectedDate.set(
                                    Calendar.YEAR,
                                    year
                            );

                            selectedDate.set(
                                    Calendar.MONTH,
                                    month
                            );

                            selectedDate.set(
                                    Calendar.DAY_OF_MONTH,
                                    dayOfMonth
                            );

                            actualizarFecha();
                        },
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    private void actualizarFecha() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "dd MMM yyyy",
                        new Locale("es", "SV")
                );

        etDate.setText(
                format.format(selectedDate.getTime())
        );
    }

    private void validarFormulario() {

        String client =
                etClient.getText().toString().trim();

        String equipment =
                etEquipment.getText().toString().trim();

        String issue =
                etIssue.getText().toString().trim();

        String technician =
                etTechnician.getText().toString().trim();

        // Limpiar errores anteriores.
        tilClient.setError(null);
        tilEquipment.setError(null);
        tilIssue.setError(null);

        boolean formularioValido = true;

        if (client.isEmpty()) {

            tilClient.setError(
                    "Selecciona un cliente"
            );

            formularioValido = false;
        }

        if (equipment.isEmpty()) {

            tilEquipment.setError(
                    "Selecciona un equipo"
            );

            formularioValido = false;
        }

        if (issue.isEmpty()) {

            tilIssue.setError(
                    "Describe el problema reportado"
            );

            formularioValido = false;
        }

        if (formularioValido) {

            // El registro real se implementará con Room/API.
            String message =
                    "Orden válida · "
                            + client
                            + " · "
                            + technician;

            Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}