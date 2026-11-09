package com.tallertech.app.ui.equipment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import com.tallertech.app.data.repository.EquipmentRepository;
import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.local.EquipmentDetails;

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
import android.widget.TextView;
import com.tallertech.app.data.repository.StatisticsRepository;
import com.tallertech.app.data.repository.ResultCallback;

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
        com.tallertech.app.data.local.LocalDataObserver.observe(this, this::reloadLocalData);

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

    private void reloadLocalData() {
        View currentView = getView();
        new StatisticsRepository(requireContext()).load(new ResultCallback<StatisticsRepository.Statistics>() {
            @Override public void onResult(StatisticsRepository.Statistics stats) {
                if (currentView == null || getView() != currentView) return;
                ((TextView) currentView.findViewById(R.id.tvTotalEquipment)).setText(String.valueOf(stats.equipment));
                ((TextView) currentView.findViewById(R.id.tvRepairEquipment)).setText(String.valueOf(stats.repairEquipment));
                ((TextView) currentView.findViewById(R.id.tvReadyEquipment)).setText(String.valueOf(stats.readyEquipment));
            }
            @Override public void onError(Exception e) { mostrarErrorCarga(currentView); }
        });
        new EquipmentRepository(requireContext()).getAllDetails(new ResultCallback<List<EquipmentDetails>>() {
            @Override
            public void onResult(List<EquipmentDetails> equipment) {
                if (!isAdded() || getView() != currentView || currentView == null) return;
                List<EquipmentUiModel> items = new ArrayList<>();
                for (EquipmentDetails details : equipment) {
                    EquipmentEntity item = details.equipment;
                    items.add(new EquipmentUiModel(item.getId(), item.getClientId(), item.getType(), item.getBrand(),
                            item.getModel(), item.getSerial(), item.getDescription(), details.clientName, details.orderStatus));
                }
                equipmentAdapter.setEquipment(items);
                equipmentAdapter.filtrar(searchQuery);
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
