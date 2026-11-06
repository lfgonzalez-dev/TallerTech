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

public class RepairFragment extends Fragment {

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
            String orderCode,
            String equipment) {

        RepairFragment fragment =
                new RepairFragment();

        Bundle args = new Bundle();

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

        MaterialButton btnSave =
                view.findViewById(R.id.btnSaveRepair);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelRepair);

        Bundle args = getArguments();

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

        String repair =
                etRepair.getText().toString().trim();

        tilRepair.setError(null);

        if (repair.isEmpty()) {

            tilRepair.setError(
                    "Describe la reparación realizada"
            );

            return;
        }

        // Guardado temporal mientras desarrollamos el frontend.
        Toast.makeText(
                requireContext(),
                "Reparación registrada correctamente",
                Toast.LENGTH_SHORT
        ).show();

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}