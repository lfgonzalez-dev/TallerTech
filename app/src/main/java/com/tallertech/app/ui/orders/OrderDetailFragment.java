package com.tallertech.app.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tallertech.app.R;
import com.tallertech.app.data.local.OrderDetails;
import com.tallertech.app.data.repository.ServiceOrderRepository;
import com.tallertech.app.data.repository.ResultCallback;

public class OrderDetailFragment extends Fragment {

    private static final String ARG_CODE = "code";
    private static final String ARG_CLIENT = "client";
    private static final String ARG_EQUIPMENT = "equipment";
    private static final String ARG_ISSUE = "issue";
    private static final String ARG_DATE = "date";
    private static final String ARG_TECHNICIAN = "technician";
    private static final String ARG_STATUS = "status";

    private TextView tvStatus;

    private String currentStatus;
    private String orderCode;
    private String equipment;
    private boolean loaded;
    private boolean saving;

    public OrderDetailFragment() {
        super(R.layout.fragment_order_detail);
    }

    public static OrderDetailFragment newInstance(
            OrderUiModel order) {

        OrderDetailFragment fragment =
                new OrderDetailFragment();

        Bundle args = new Bundle();
        args.putString("id", order.getId());

        args.putString(ARG_CODE, order.getCode());
        args.putString(ARG_CLIENT, order.getClient());
        args.putString(ARG_EQUIPMENT, order.getEquipment());
        args.putString(ARG_ISSUE, order.getIssue());
        args.putString(ARG_DATE, order.getDate());
        args.putString(ARG_TECHNICIAN, order.getTechnician());
        args.putString(ARG_STATUS, order.getStatus());

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        com.tallertech.app.data.local.LocalDataObserver.observe(this, this::reloadLocalData);
        loaded = false;
        saving = false;

        TextView tvCode =
                view.findViewById(R.id.tvDetailOrderCode);

        TextView tvClient =
                view.findViewById(R.id.tvDetailOrderClient);

        TextView tvEquipment =
                view.findViewById(R.id.tvDetailOrderEquipment);

        TextView tvIssue =
                view.findViewById(R.id.tvDetailOrderIssue);

        TextView tvDate =
                view.findViewById(R.id.tvDetailOrderDate);

        TextView tvTechnician =
                view.findViewById(R.id.tvDetailOrderTechnician);

        tvStatus =
                view.findViewById(R.id.tvDetailOrderStatus);

        MaterialButton btnBack =
                view.findViewById(R.id.btnBackOrderDetail);

        MaterialButton btnChangeStatus =
                view.findViewById(R.id.btnChangeOrderStatus);

        MaterialButton btnDiagnosis =
                view.findViewById(R.id.btnOrderDiagnosis);

        MaterialButton btnRepair =
                view.findViewById(R.id.btnOrderRepair);

        Bundle args = getArguments();

        if (args != null) {

            orderCode =
                    args.getString(ARG_CODE, "");

            equipment =
                    args.getString(ARG_EQUIPMENT, "");

            currentStatus =
                    args.getString(
                            ARG_STATUS,
                            "Pendiente"
                    );

            tvCode.setText(orderCode);

            tvClient.setText(
                    args.getString(ARG_CLIENT, "")
            );

            tvEquipment.setText(equipment);

            tvIssue.setText(
                    args.getString(ARG_ISSUE, "")
            );

            tvDate.setText(
                    args.getString(ARG_DATE, "")
            );

            tvTechnician.setText(
                    args.getString(ARG_TECHNICIAN, "")
            );

            tvStatus.setText(currentStatus);
        }

        // Regresar a órdenes.
        btnBack.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        // Cambiar estado de la orden.
        btnChangeStatus.setOnClickListener(v ->
                mostrarSelectorEstado()
        );

        // Registrar diagnóstico.
        btnDiagnosis.setOnClickListener(v ->
                abrirDiagnostico()
        );

        // Registrar reparación.
        btnRepair.setOnClickListener(v ->
                abrirReparacion()
        );
    }

    private void abrirDiagnostico() {

        DiagnosisFragment fragment =
                DiagnosisFragment.newInstance(
                        requireArguments().getString("id"),
                        orderCode,
                        equipment
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .addToBackStack(null)
                .commit();
    }

    private void abrirReparacion() {

        RepairFragment fragment =
                RepairFragment.newInstance(
                        requireArguments().getString("id"),
                        orderCode,
                        equipment
                );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        fragment
                )
                .addToBackStack(null)
                .commit();
    }

    private void mostrarSelectorEstado() {
        if (!loaded || saving) return;

        String[] states = {
                "Pendiente",
                "Diagnóstico",
                "En reparación",
                "Completado"
        };

        int selectedPosition =
                obtenerPosicionEstado(states);

        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle("Cambiar estado")
                .setSingleChoiceItems(
                        states,
                        selectedPosition,
                        null
                )
                .setPositiveButton(
                        "Guardar",
                        (dialog, which) -> {

                            int position =
                                    ((androidx.appcompat.app.AlertDialog) dialog)
                                            .getListView()
                                            .getCheckedItemPosition();

                            if (position >= 0) {

                                guardarEstado(states[position]);
                            }
                        }
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .show();
    }

    private int obtenerPosicionEstado(
            String[] states) {

        for (int i = 0; i < states.length; i++) {

            if (states[i]
                    .equalsIgnoreCase(currentStatus)) {

                return i;
            }
        }

        return 0;
    }

    private void reloadLocalData() {
        View view = getView();
        if (view == null) return;
        setActionsEnabled(view, false);
        new ServiceOrderRepository(requireContext()).getById(requireArguments().getString("id"), new ResultCallback<OrderDetails>() {
            @Override public void onResult(OrderDetails result) {
                if (!isAdded() || getView() != view) return;
                OrderUiModel order = OrderUiModel.fromDetails(result);
                orderCode = order.getCode();
                equipment = order.getEquipment();
                currentStatus = order.getStatus();
                ((TextView) view.findViewById(R.id.tvDetailOrderCode)).setText(orderCode);
                ((TextView) view.findViewById(R.id.tvDetailOrderClient)).setText(order.getClient());
                ((TextView) view.findViewById(R.id.tvDetailOrderEquipment)).setText(equipment);
                ((TextView) view.findViewById(R.id.tvDetailOrderIssue)).setText(order.getIssue());
                ((TextView) view.findViewById(R.id.tvDetailOrderDate)).setText(order.getDate());
                ((TextView) view.findViewById(R.id.tvDetailOrderTechnician)).setText(order.getTechnician());
                tvStatus.setText(currentStatus);
                loaded = true;
                setActionsEnabled(view, true);
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != view) return;
                Toast.makeText(requireContext(), "Error al cargar orden", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setActionsEnabled(View view, boolean enabled) {
        view.findViewById(R.id.btnChangeOrderStatus).setEnabled(enabled);
        view.findViewById(R.id.btnOrderDiagnosis).setEnabled(enabled);
        view.findViewById(R.id.btnOrderRepair).setEnabled(enabled);
    }

    private void guardarEstado(String status) {
        saving = true;
        View view = getView();
        setActionsEnabled(view, false);
        new ServiceOrderRepository(requireContext()).updateStatus(requireArguments().getString("id"), status, new ResultCallback<Void>() {
            @Override public void onResult(Void result) {
                if (!isAdded() || getView() != view) return;
                saving = false;
                currentStatus = status;
                tvStatus.setText(status);
                setActionsEnabled(view, true);
                Toast.makeText(requireContext(), "Estado actualizado a " + status, Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != view) return;
                saving = false;
                setActionsEnabled(view, true);
                Toast.makeText(requireContext(), "Error al cambiar estado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
