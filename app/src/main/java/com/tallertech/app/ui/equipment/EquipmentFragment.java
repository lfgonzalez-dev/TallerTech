package com.tallertech.app.ui.equipment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

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

                        equipmentAdapter
                                .filtrar(s.toString());
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
                crearEquiposPrueba();

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

    private List<EquipmentUiModel> crearEquiposPrueba() {

        List<EquipmentUiModel> equipment =
                new ArrayList<>();

        // Datos temporales mientras desarrollamos el frontend.
        equipment.add(new EquipmentUiModel(
                "Laptop",
                "Dell",
                "Inspiron 15",
                "DL-98541",
                "No enciende y presenta desgaste exterior.",
                "María López",
                "En reparación"
        ));

        equipment.add(new EquipmentUiModel(
                "Smartphone",
                "Samsung",
                "Galaxy A54",
                "SM-A546",
                "Pantalla dañada.",
                "Carlos Méndez",
                "Completado"
        ));

        equipment.add(new EquipmentUiModel(
                "Laptop",
                "Lenovo",
                "IdeaPad 3",
                "LN-45681",
                "Falla de carga.",
                "Ana Torres",
                "Diagnóstico"
        ));

        equipment.add(new EquipmentUiModel(
                "Tablet",
                "Samsung",
                "Galaxy Tab S8",
                "TB-10294",
                "Problema con el puerto USB.",
                "Roberto Díaz",
                "En reparación"
        ));

        equipment.add(new EquipmentUiModel(
                "Computadora",
                "HP",
                "ProDesk 400",
                "HP-78421",
                "Mantenimiento preventivo.",
                "Sofía Guzmán",
                "Completado"
        ));

        return equipment;
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