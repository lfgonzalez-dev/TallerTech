package com.tallertech.app.ui.orders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tallertech.app.R;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;

    public OrdersFragment() {
        super(R.layout.fragment_orders);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Referencias a la interfaz.
        rvOrders =
                view.findViewById(R.id.rvOrders);

        TextInputEditText etSearchOrder =
                view.findViewById(R.id.etSearchOrder);

        Chip chipAll =
                view.findViewById(R.id.chipAllOrders);

        Chip chipPending =
                view.findViewById(R.id.chipPendingOrders);

        Chip chipDiagnosis =
                view.findViewById(R.id.chipDiagnosisOrders);

        Chip chipRepair =
                view.findViewById(R.id.chipRepairOrders);

        ExtendedFloatingActionButton fabNewOrder =
                view.findViewById(R.id.fabNewOrder);

        configurarRecyclerView();

        // Buscar mientras el usuario escribe.
        etSearchOrder.addTextChangedListener(
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

                        orderAdapter
                                .filtrarTexto(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        // Filtros por estado.
        chipAll.setOnClickListener(v ->
                orderAdapter.filtrarEstado("Todas")
        );

        chipPending.setOnClickListener(v ->
                orderAdapter.filtrarEstado("Pendiente")
        );

        chipDiagnosis.setOnClickListener(v ->
                orderAdapter.filtrarEstado("Diagnóstico")
        );

        chipRepair.setOnClickListener(v ->
                orderAdapter.filtrarEstado("En reparación")
        );

        // Abrir el formulario de nueva orden.
        fabNewOrder.setOnClickListener(v ->
                abrirNuevaOrden()
        );
    }

    private void configurarRecyclerView() {

        List<OrderUiModel> orders =
                crearOrdenesPrueba();

        rvOrders.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        orderAdapter =
                new OrderAdapter(
                        orders,
                        this::abrirDetalleOrden
                );

        rvOrders.setAdapter(
                orderAdapter
        );
    }

    private List<OrderUiModel> crearOrdenesPrueba() {

        List<OrderUiModel> orders =
                new ArrayList<>();

        // Datos temporales mientras desarrollamos el frontend.
        orders.add(new OrderUiModel(
                "ORD-0241",
                "María López",
                "Dell Inspiron 15",
                "No enciende después de una subida de voltaje.",
                "12 sep 2026",
                "Téc. Carlos Reyes",
                "En reparación"
        ));

        orders.add(new OrderUiModel(
                "ORD-0242",
                "Carlos Méndez",
                "Samsung Galaxy A54",
                "Pantalla quebrada y fallas táctiles.",
                "15 sep 2026",
                "Téc. Andrea Martínez",
                "Diagnóstico"
        ));

        orders.add(new OrderUiModel(
                "ORD-0243",
                "Roberto Díaz",
                "HP 240 G8",
                "El equipo se apaga después de algunos minutos.",
                "17 sep 2026",
                "Sin asignar",
                "Pendiente"
        ));

        orders.add(new OrderUiModel(
                "ORD-0244",
                "Ana Torres",
                "Lenovo IdeaPad 3",
                "No reconoce el cargador.",
                "18 sep 2026",
                "Téc. Carlos Reyes",
                "Diagnóstico"
        ));

        orders.add(new OrderUiModel(
                "ORD-0245",
                "Sofía Guzmán",
                "HP ProDesk 400",
                "Mantenimiento preventivo general.",
                "19 sep 2026",
                "Téc. Andrea Martínez",
                "En reparación"
        ));

        return orders;
    }

    private void abrirNuevaOrden() {

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new NewOrderFragment()
                )
                .addToBackStack(null)
                .commit();
    }
    private void abrirDetalleOrden(
            OrderUiModel order) {

        // Abrir la orden seleccionada.
        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        OrderDetailFragment
                                .newInstance(order)
                )
                .addToBackStack(null)
                .commit();
    }
}