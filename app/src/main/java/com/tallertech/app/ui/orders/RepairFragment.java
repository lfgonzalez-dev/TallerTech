package com.tallertech.app.ui.orders;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tallertech.app.R;
import com.tallertech.app.data.local.entity.RepairEntity;
import com.tallertech.app.data.repository.RepairRepository;
import com.tallertech.app.data.repository.ResultCallback;

public class RepairFragment extends Fragment {
    private MaterialButton btnSave;
    private boolean loaded, saving;

    private static final String ARG_ORDER_CODE = "orderCode";
    private static final String ARG_EQUIPMENT = "equipment";

    private TextInputLayout tilRepair;
    private TextInputEditText etRepair;
    private TextInputEditText etParts;
    private TextInputEditText etObservations;

    public RepairFragment() {
        super(R.layout.fragment_repair);
    }

    public static RepairFragment newInstance(
            String orderId,
            String orderCode,
            String equipment) {

        RepairFragment fragment =
                new RepairFragment();

        Bundle args = new Bundle();
        args.putString("orderId", orderId);

        args.putString(ARG_ORDER_CODE, orderCode);
        args.putString(ARG_EQUIPMENT, equipment);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView tvOrder =
                view.findViewById(R.id.tvRepairOrder);

        TextView tvEquipment =
                view.findViewById(R.id.tvRepairEquipment);

        tilRepair =
                view.findViewById(R.id.tilRepair);

        etRepair =
                view.findViewById(R.id.etRepair);

        etParts =
                view.findViewById(R.id.etRepairParts);

        etObservations =
                view.findViewById(R.id.etRepairObservations);

        btnSave =
                view.findViewById(R.id.btnSaveRepair);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelRepair);

        Bundle args = getArguments();
        loaded = false;
        saving = false;
        btnSave.setEnabled(false);
        etRepair.setEnabled(false);
        etParts.setEnabled(false);
        etObservations.setEnabled(false);
        new RepairRepository(requireContext()).getByOrder(requireArguments().getString("orderId"), new ResultCallback<RepairEntity>() {
            @Override public void onResult(RepairEntity result) {
                if (!isAdded() || getView() != view) return;
                if (result != null && savedInstanceState == null) {
                    etRepair.setText(result.getRepair());
                    etParts.setText(result.getParts());
                    etObservations.setText(result.getObservations());
                }
                loaded = true;
                btnSave.setEnabled(true);
                etRepair.setEnabled(true);
                etParts.setEnabled(true);
                etObservations.setEnabled(true);
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != view) return;
                Toast.makeText(requireContext(), "Error al cargar reparación", Toast.LENGTH_SHORT).show();
            }
        });

        if (args != null) {

            tvOrder.setText(
                    args.getString(ARG_ORDER_CODE, "")
            );

            tvEquipment.setText(
                    args.getString(ARG_EQUIPMENT, "")
            );
        }

        btnSave.setOnClickListener(v ->
                validarFormulario()
        );

        btnCancel.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
    }

    private void validarFormulario() {
        if (!loaded || saving) return;

        String repair =
                etRepair.getText().toString().trim();

        tilRepair.setError(null);

        if (repair.isEmpty()) {

            tilRepair.setError(
                    "Describe la reparación realizada"
            );

            return;
        }

        saving = true;
        btnSave.setEnabled(false);
        View view = getView();
        new RepairRepository(requireContext()).save(requireArguments().getString("orderId"), repair,
                etParts.getText().toString().trim(), etObservations.getText().toString().trim(), new ResultCallback<Void>() {
                    @Override public void onResult(Void result) {
                        if (!isAdded() || getView() != view) return;
                        Toast.makeText(requireContext(), "Reparación registrada correctamente", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                    @Override public void onError(Exception e) {
                        if (!isAdded() || getView() != view) return;
                        saving = false;
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(), "Error al guardar reparación", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
