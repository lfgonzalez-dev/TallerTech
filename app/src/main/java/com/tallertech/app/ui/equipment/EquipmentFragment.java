package com.tallertech.app.ui.equipment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import com.tallertech.app.data.repository.EquipmentRepository;
import com.tallertech.app.data.repository.ClientRepository;
import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.local.entity.ClientEntity;
import java.util.Map;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tallertech.app.R;

import java.util.ArrayList;
import java.util.List;

public class EquipmentFragment extends Fragment {

    private RecyclerView rvEquipment;
    private EquipmentAdapter equipmentAdapter;
    private String searchQuery = "";

    public EquipmentFragment() {
        super(R.layout.fragment_equipment);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Referencias a los componentes de la pantalla.
        rvEquipment =
                view.findViewById(R.id.rvEquipment);

        TextInputEditText etSearchEquipment =
                view.findViewById(R.id.etSearchEquipment);

        ExtendedFloatingActionButton fabNewEquipment =
                view.findViewById(R.id.fabNewEquipment);

        // Configurar la lista de equipos.
        configurarRecyclerView();

        // Filtrar equipos mientras el usuario escribe.
        etSearchEquipment.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        searchQuery = s.toString();
                        equipmentAdapter.filtrar(searchQuery);
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        // Abrir formulario para registrar un equipo.
        fabNewEquipment.setOnClickListener(v ->
                abrirRegistroEquipo()
        );
    }

    private void configurarRecyclerView() {

        List<EquipmentUiModel> equipment =
                new ArrayList<>();

        // Mostrar los equipos verticalmente.
        rvEquipment.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        // El Adapter también recibe la acción al tocar un equipo.
        equipmentAdapter =
                new EquipmentAdapter(
                        equipment,
                        this::abrirDetalleEquipo
                );

        rvEquipment.setAdapter(
                equipmentAdapter
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        View currentView = getView();
        new ClientRepository(requireContext()).getAll(new ClientRepository.ClientsCallback() {
            @Override
            public void onResult(List<ClientEntity> clients) {
                if (!isAdded() || getView() != currentView || currentView == null) return;
                Map<String, String> names = new HashMap<>();
                for (ClientEntity client : clients) names.put(client.getId(), client.getName());
                new EquipmentRepository(requireContext()).getAll(new EquipmentRepository.EquipmentCallback() {
                    @Override
                    public void onResult(List<EquipmentEntity> equipment) {
                        if (!isAdded() || getView() != currentView || currentView == null) return;
                        List<EquipmentUiModel> items = new ArrayList<>();
                        for (EquipmentEntity item : equipment) {
                            items.add(new EquipmentUiModel(item.getType(), item.getBrand(),
                                    item.getModel(), item.getSerial(), item.getDescription(),
                                    names.getOrDefault(item.getClientId(), ""), "Registrado"));
                        }
                        equipmentAdapter.setEquipment(items);
                        equipmentAdapter.filtrar(searchQuery);
                    }
                    @Override
                    public void onError(Exception e) { mostrarErrorCarga(currentView); }
                });
            }
            @Override
            public void onError(Exception e) { mostrarErrorCarga(currentView); }
        });
    }

    private void mostrarErrorCarga(View currentView) {
        if (!isAdded() || getView() != currentView || currentView == null) return;
        Toast.makeText(requireContext(), "Error al cargar equipos", Toast.LENGTH_SHORT).show();
    }

    private void abrirRegistroEquipo() {

        // Abrir formulario de registro.
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new RegisterEquipmentFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    private void abrirDetalleEquipo(
            EquipmentUiModel equipment) {

        // Mostrar la información del equipo seleccionado.
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        EquipmentDetailFragment
                                .newInstance(equipment)
                )
                .addToBackStack(null)
                .commit();
    }
}