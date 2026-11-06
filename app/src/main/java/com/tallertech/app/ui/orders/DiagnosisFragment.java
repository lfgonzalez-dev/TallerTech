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

public class DiagnosisFragment extends Fragment {

    private static final String ARG_ORDER_CODE = "orderCode";
    private static final String ARG_EQUIPMENT = "equipment";

    private TextInputLayout tilDiagnosis;
    private TextInputEditText etDiagnosis;
    private TextInputEditText etObservations;
    private TextInputEditText etRecommendations;

    public DiagnosisFragment() {
        super(R.layout.fragment_diagnosis);
    }

    public static DiagnosisFragment newInstance(
            String orderCode,
            String equipment) {

        DiagnosisFragment fragment =
                new DiagnosisFragment();

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
                view.findViewById(R.id.tvDiagnosisOrder);

        TextView tvEquipment =
                view.findViewById(R.id.tvDiagnosisEquipment);

        tilDiagnosis =
                view.findViewById(R.id.tilDiagnosis);

        etDiagnosis =
                view.findViewById(R.id.etDiagnosis);

        etObservations =
                view.findViewById(R.id.etDiagnosisObservations);

        etRecommendations =
                view.findViewById(R.id.etDiagnosisRecommendations);

        MaterialButton btnSave =
                view.findViewById(R.id.btnSaveDiagnosis);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelDiagnosis);

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

        String diagnosis =
                etDiagnosis.getText().toString().trim();

        tilDiagnosis.setError(null);

        if (diagnosis.isEmpty()) {

            tilDiagnosis.setError(
                    "Ingresa el diagnóstico técnico"
            );

            return;
        }

        // Por ahora simulamos el guardado.
        Toast.makeText(
                requireContext(),
                "Diagnóstico registrado correctamente",
                Toast.LENGTH_SHORT
        ).show();

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }
}