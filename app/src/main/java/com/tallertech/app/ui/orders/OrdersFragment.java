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
import com.tallertech.app.data.local.OrderDetails;
import com.tallertech.app.data.repository.ServiceOrderRepository;
import com.tallertech.app.data.repository.ResultCallback;
import android.widget.Toast;

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
                new ArrayList<>();

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

    @Override public void onResume() {
        super.onResume();
        View view = getView();
        new ServiceOrderRepository(requireContext()).getAll(new ResultCallback<List<OrderDetails>>() {
            @Override public void onResult(List<OrderDetails> orders) {
                if (!isAdded() || getView() != view || view == null) return;
                List<OrderUiModel> items = new ArrayList<>();
                for (OrderDetails order : orders) items.add(OrderUiModel.fromDetails(order));
                orderAdapter.setOrders(items);
                TextInputEditText search = view.findViewById(R.id.etSearchOrder);
                orderAdapter.filtrarTexto(search.getText().toString());
                com.google.android.material.chip.ChipGroup group = view.findViewById(R.id.chipGroupOrders);
                int selected = group.getCheckedChipId();
                orderAdapter.filtrarEstado(selected == R.id.chipPendingOrders ? "Pendiente" :
                        selected == R.id.chipDiagnosisOrders ? "Diagnóstico" :
                        selected == R.id.chipRepairOrders ? "En reparación" : "Todas");
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != view || view == null) return;
                Toast.makeText(requireContext(), "Error al cargar órdenes", Toast.LENGTH_SHORT).show();
            }
        });
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
