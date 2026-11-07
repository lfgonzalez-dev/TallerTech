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
import com.tallertech.app.data.local.entity.DiagnosisEntity;
import com.tallertech.app.data.repository.DiagnosisRepository;
import com.tallertech.app.data.repository.ResultCallback;

public class DiagnosisFragment extends Fragment {
    private MaterialButton btnSave;
    private boolean loaded, saving;

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
            String orderId,
            String orderCode,
            String equipment) {

        DiagnosisFragment fragment =
                new DiagnosisFragment();

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

        btnSave =
                view.findViewById(R.id.btnSaveDiagnosis);

        MaterialButton btnCancel =
                view.findViewById(R.id.btnCancelDiagnosis);

        Bundle args = getArguments();
        loaded = false;
        saving = false;
        btnSave.setEnabled(false);
        etDiagnosis.setEnabled(false);
        etObservations.setEnabled(false);
        etRecommendations.setEnabled(false);
        new DiagnosisRepository(requireContext()).getByOrder(requireArguments().getString("orderId"), new ResultCallback<DiagnosisEntity>() {
            @Override public void onResult(DiagnosisEntity result) {
                if (!isAdded() || getView() != view) return;
                if (result != null && savedInstanceState == null) {
                    etDiagnosis.setText(result.getDiagnosis());
                    etObservations.setText(result.getObservations());
                    etRecommendations.setText(result.getRecommendations());
                }
                loaded = true;
                btnSave.setEnabled(true);
                etDiagnosis.setEnabled(true);
                etObservations.setEnabled(true);
                etRecommendations.setEnabled(true);
            }
            @Override public void onError(Exception e) {
                if (!isAdded() || getView() != view) return;
                Toast.makeText(requireContext(), "Error al cargar diagnóstico", Toast.LENGTH_SHORT).show();
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

        String diagnosis =
                etDiagnosis.getText().toString().trim();

        tilDiagnosis.setError(null);

        if (diagnosis.isEmpty()) {

            tilDiagnosis.setError(
                    "Ingresa el diagnóstico técnico"
            );

            return;
        }

        saving = true;
        btnSave.setEnabled(false);
        View view = getView();
        new DiagnosisRepository(requireContext()).save(requireArguments().getString("orderId"), diagnosis,
                etObservations.getText().toString().trim(), etRecommendations.getText().toString().trim(), new ResultCallback<Void>() {
                    @Override public void onResult(Void result) {
                        if (!isAdded() || getView() != view) return;
                        Toast.makeText(requireContext(), "Diagnóstico registrado correctamente", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                    @Override public void onError(Exception e) {
                        if (!isAdded() || getView() != view) return;
                        saving = false;
                        btnSave.setEnabled(true);
                        Toast.makeText(requireContext(), "Error al guardar diagnóstico", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
