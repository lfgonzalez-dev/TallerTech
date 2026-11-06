package com.tallertech.app.ui.equipment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tallertech.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EquipmentAdapter
        extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private final List<EquipmentUiModel> equipment;
    private final List<EquipmentUiModel> filteredEquipment;

    private final OnEquipmentClickListener listener;

    // Informa qué equipo fue seleccionado.
    public interface OnEquipmentClickListener {
        void onEquipmentClick(EquipmentUiModel equipment);
    }

    public EquipmentAdapter(
            List<EquipmentUiModel> equipment,
            OnEquipmentClickListener listener) {

        this.equipment = new ArrayList<>(equipment);
        this.filteredEquipment = new ArrayList<>(equipment);
        this.listener = listener;
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        // Crear una fila utilizando item_equipment.xml.
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_equipment,
                        parent,
                        false
                );

        return new EquipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull EquipmentViewHolder holder,
            int position) {

        EquipmentUiModel item =
                filteredEquipment.get(position);

        holder.tvType.setText(
                obtenerAbreviatura(item.getType())
        );

        holder.tvName.setText(
                item.getFullName()
        );

        holder.tvClient.setText(
                item.getClient()
                        + " · "
                        + item.getSerial()
        );

        holder.tvStatus.setText(
                item.getStatus()
        );

        // Abrir detalle del equipo seleccionado.
        holder.itemView.setOnClickListener(v ->
                listener.onEquipmentClick(item)
        );
    }

    @Override
    public int getItemCount() {
        return filteredEquipment.size();
    }

    public void filtrar(String texto) {

        filteredEquipment.clear();

        String query = texto
                .trim()
                .toLowerCase(Locale.ROOT);

        if (query.isEmpty()) {

            filteredEquipment.addAll(equipment);

        } else {

            for (EquipmentUiModel item : equipment) {

                String searchable =
                        item.getType() + " " +
                                item.getBrand() + " " +
                                item.getModel() + " " +
                                item.getSerial() + " " +
                                item.getClient() + " " +
                                item.getStatus();

                if (searchable
                        .toLowerCase(Locale.ROOT)
                        .contains(query)) {

                    filteredEquipment.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    private String obtenerAbreviatura(String type) {

        if (type == null || type.isEmpty()) {
            return "EQ";
        }

        String normalized =
                type.toLowerCase(Locale.ROOT);

        if (normalized.contains("laptop")) {
            return "LT";
        }

        if (normalized.contains("smartphone")
                || normalized.contains("teléfono")) {

            return "SP";
        }

        if (normalized.contains("tablet")) {
            return "TB";
        }

        if (normalized.contains("desktop")
                || normalized.contains("computadora")) {

            return "PC";
        }

        return "EQ";
    }

    static class EquipmentViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvType;
        TextView tvName;
        TextView tvClient;
        TextView tvStatus;

        public EquipmentViewHolder(
                @NonNull View itemView) {

            super(itemView);

            tvType =
                    itemView.findViewById(
                            R.id.tvEquipmentType
                    );

            tvName =
                    itemView.findViewById(
                            R.id.tvEquipmentName
                    );

            tvClient =
                    itemView.findViewById(
                            R.id.tvEquipmentClient
                    );

            tvStatus =
                    itemView.findViewById(
                            R.id.tvEquipmentStatus
                    );
        }
    }
}