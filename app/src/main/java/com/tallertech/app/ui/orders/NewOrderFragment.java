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
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.repository.ClientRepository;
import com.tallertech.app.data.repository.EquipmentRepository;
import com.tallertech.app.data.repository.ServiceOrderRepository;
import com.tallertech.app.data.repository.ResultCallback;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewOrderFragment extends Fragment {
    private AutoCompleteTextView etClient, etEquipment, etTechnician;
    private TextInputEditText etIssue, etDate;
    private TextInputLayout tilClient, tilEquipment, tilIssue;
    private MaterialButton btnCreate;
    private ClientEntity selectedClient;
    private EquipmentEntity selectedEquipment;
    private boolean saving;
    private final Calendar selectedDate = Calendar.getInstance();

    private static class ClientOption {
        final ClientEntity client;
        ClientOption(ClientEntity client) { this.client = client; }
        @NonNull @Override public String toString() { return client.getName() + " · " + client.getPhone(); }
    }

    private static class EquipmentOption {
        final EquipmentEntity equipment;
        EquipmentOption(EquipmentEntity equipment) { this.equipment = equipment; }
        @NonNull @Override public String toString() {
            return equipment.getBrand() + " " + equipment.getModel() + " · " + equipment.getSerial();
        }
    }

    public NewOrderFragment() { super(R.layout.fragment_new_order); }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        tilClient = view.findViewById(R.id.tilOrderClient);
        tilEquipment = view.findViewById(R.id.tilOrderEquipment);
        tilIssue = view.findViewById(R.id.tilOrderIssue);
        etClient = view.findViewById(R.id.etOrderClient);
        etEquipment = view.findViewById(R.id.etOrderEquipment);
        etTechnician = view.findViewById(R.id.etOrderTechnician);
        etIssue = view.findViewById(R.id.etOrderIssue);
        etDate = view.findViewById(R.id.etOrderDate);
        btnCreate = view.findViewById(R.id.btnCreateOrder);
        selectedClient = null;
        selectedEquipment = null;
        saving = false;
        etClient.setEnabled(false);
        etEquipment.setEnabled(false);
        etTechnician.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        var currentUser = com.tallertech.app.data.repository.AuthRepository.getCurrentUser();
        etTechnician.setText(currentUser == null ? "" : currentUser.name, false);
        actualizarFecha();
        etDate.setOnClickListener(v -> new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            selectedDate.set(year, month, day);
            actualizarFecha();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)).show());
        etClient.setOnItemClickListener((parent, v, position, id) -> {
            selectedClient = ((ClientOption) parent.getItemAtPosition(position)).client;
            tilClient.setError(null);
            cargarEquipos(selectedClient.getId(), view);
        });
        etEquipment.setOnItemClickListener((parent, v, position, id) -> {
            selectedEquipment = ((EquipmentOption) parent.getItemAtPosition(position)).equipment;
            tilEquipment.setError(null);
        });
        new ClientRepository(requireContext()).getAll(new ClientRepository.ClientsCallback() {
            @Override public void onResult(List<ClientEntity> clients) {
                if (!isAdded() || getView() != view) return;
                List<ClientOption> options = new ArrayList<>();
                for (ClientEntity client : clients) options.add(new ClientOption(client));
                etClient.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, options));
                etClient.setEnabled(!clients.isEmpty());
                if (clients.isEmpty()) tilClient.setError("Registra un cliente antes de crear una orden");
            }
            @Override public void onError(Exception e) {
                if (getView() == view) tilClient.setError("Error al cargar clientes");
            }
        });
        btnCreate.setOnClickListener(v -> guardar());
        view.findViewById(R.id.btnCancelOrder).setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void cargarEquipos(String clientId, View view) {
        selectedEquipment = null;
        etEquipment.setText("", false);
        etEquipment.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>()));
        etEquipment.setEnabled(false);
        new EquipmentRepository(requireContext()).getByClient(clientId, new ResultCallback<List<EquipmentEntity>>() {
            @Override public void onResult(List<EquipmentEntity> equipment) {
                if (!isAdded() || getView() != view || selectedClient == null || !clientId.equals(selectedClient.getId())) return;
                List<EquipmentOption> options = new ArrayList<>();
                for (EquipmentEntity item : equipment) options.add(new EquipmentOption(item));
                etEquipment.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, options));
                etEquipment.setEnabled(!equipment.isEmpty());
                tilEquipment.setError(equipment.isEmpty() ? "Este cliente no tiene equipos registrados" : null);
            }
            @Override public void onError(Exception e) {
                if (getView() == view && selectedClient != null && clientId.equals(selectedClient.getId())) {
                    tilEquipment.setError("Error al cargar equipos");
                }
            }
        });
    }

    private void actualizarFecha() {
        etDate.setText(new SimpleDateFormat("dd MMM yyyy", new Locale("es", "SV")).format(selectedDate.getTime()));
    }

    private void guardar() {
        if (saving) return;
        tilClient.setError(null);
        tilEquipment.setError(null);
        tilIssue.setError(null);
        boolean valid = true;
        if (selectedClient == null || !etClient.getText().toString().equals(new ClientOption(selectedClient).toString())) {
            tilClient.setError("Selecciona un cliente");
            valid = false;
        }
        if (selectedEquipment == null || selectedClient == null || !selectedEquipment.getClientId().equals(selectedClient.getId())
                || !etEquipment.getText().toString().equals(new EquipmentOption(selectedEquipment).toString())) {
            tilEquipment.setError("Selecciona un equipo del cliente");
            valid = false;
        }
        String issue = etIssue.getText().toString().trim();
        if (issue.isEmpty()) {
            tilIssue.setError("Describe el problema reportado");
            valid = false;
        }
        if (!valid) return;
        saving = true;
        btnCreate.setEnabled(false);
        String technician = etTechnician.getText().toString().trim();
        View view = getView();
        new ServiceOrderRepository(requireContext()).create(selectedClient.getId(), selectedEquipment.getId(), issue,
                selectedDate.getTimeInMillis(), technician.isEmpty() ? "Sin asignar" : technician, new ResultCallback<String>() {
                    @Override public void onResult(String code) {
                        if (!isAdded() || getView() != view) return;
                        Toast.makeText(requireContext(), "Orden registrada: " + code, Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                    @Override public void onError(Exception e) {
                        if (!isAdded() || getView() != view) return;
                        saving = false;
                        btnCreate.setEnabled(true);
                        Toast.makeText(requireContext(), "Error al guardar la orden; verifica cliente y equipo", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
