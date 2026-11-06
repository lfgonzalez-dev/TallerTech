package com.tallertech.app.ui.equipment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.tallertech.app.R;
import com.tallertech.app.ui.orders.NewOrderFragment;

public class EquipmentDetailFragment extends Fragment {

    private static final String ARG_TYPE = "type";
    private static final String ARG_BRAND = "brand";
    private static final String ARG_MODEL = "model";
    private static final String ARG_SERIAL = "serial";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_CLIENT = "client";
    private static final String ARG_STATUS = "status";

    public EquipmentDetailFragment() {
        super(R.layout.fragment_equipment_detail);
    }

    // Crea el Fragment con los datos del equipo seleccionado.
    public static EquipmentDetailFragment newInstance(
            EquipmentUiModel equipment) {

        EquipmentDetailFragment fragment =
                new EquipmentDetailFragment();

        Bundle args = new Bundle();

        args.putString(ARG_TYPE, equipment.getType());
        args.putString(ARG_BRAND, equipment.getBrand());
        args.putString(ARG_MODEL, equipment.getModel());
        args.putString(ARG_SERIAL, equipment.getSerial());
        args.putString(ARG_DESCRIPTION, equipment.getDescription());
        args.putString(ARG_CLIENT, equipment.getClient());
        args.putString(ARG_STATUS, equipment.getStatus());

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView tvName =
                view.findViewById(R.id.tvDetailEquipmentName);

        TextView tvType =
                view.findViewById(R.id.tvDetailEquipmentType);

        TextView tvSerial =
                view.findViewById(R.id.tvDetailEquipmentSerial);

        TextView tvClient =
                view.findViewById(R.id.tvDetailEquipmentClient);

        TextView tvStatus =
                view.findViewById(R.id.tvDetailEquipmentStatus);

        TextView tvDescription =
                view.findViewById(R.id.tvDetailEquipmentDescription);

        MaterialButton btnBack =
                view.findViewById(R.id.btnBackEquipmentDetail);

        MaterialButton btnEdit =
                view.findViewById(R.id.btnEditEquipment);

        MaterialButton btnNewOrder =
                view.findViewById(R.id.btnNewOrderEquipment);

        Bundle args = getArguments();

        if (args != null) {

            String brand =
                    args.getString(ARG_BRAND, "");

            String model =
                    args.getString(ARG_MODEL, "");

            tvName.setText(brand + " " + model);

            tvType.setText(
                    args.getString(ARG_TYPE, "")
            );

            tvSerial.setText(
                    args.getString(ARG_SERIAL, "")
            );

            tvClient.setText(
                    args.getString(ARG_CLIENT, "")
            );

            tvStatus.setText(
                    args.getString(ARG_STATUS, "")
            );

            tvDescription.setText(
                    args.getString(ARG_DESCRIPTION, "")
            );
        }

        // Regresar a Equipos.
        btnBack.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        // Editar será conectado en el siguiente paso.
        btnEdit.setOnClickListener(v ->
                abrirEditarEquipo()
        );

        // Abrir nueva orden desde este equipo.
        btnNewOrder.setOnClickListener(v ->
                abrirNuevaOrden()
        );
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
    private void abrirEditarEquipo() {

        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        EditEquipmentFragment fragment =
                EditEquipmentFragment.newInstance(
                        args.getString(ARG_CLIENT, ""),
                        args.getString(ARG_TYPE, ""),
                        args.getString(ARG_BRAND, ""),
                        args.getString(ARG_MODEL, ""),
                        args.getString(ARG_SERIAL, ""),
                        args.getString(ARG_DESCRIPTION, "")
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
}