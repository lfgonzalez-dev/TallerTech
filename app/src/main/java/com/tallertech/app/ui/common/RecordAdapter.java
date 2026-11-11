package com.tallertech.app.ui.common;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tallertech.app.R;
import java.util.*;

public final class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.Holder> {
    /** Display fields only; data loading and click actions remain in each Fragment. */
    public static final class Row {
        final String title, details, badge, equipment, date, technician, diagnosis, repair;
        final boolean user;
        final Runnable click;

        private Row(boolean user, String title, String details, String badge, String equipment,
                    String date, String technician, String diagnosis, String repair, Runnable click) {
            this.user = user; this.title = title; this.details = details; this.badge = badge;
            this.equipment = equipment; this.date = date; this.technician = technician;
            this.diagnosis = diagnosis; this.repair = repair; this.click = click;
        }

        public static Row history(String code, String status, String equipment, String client,
                                  String date, String technician, String diagnosis, String repair, Runnable click) {
            return new Row(false, code, client, status, equipment, date, technician, diagnosis, repair, click);
        }

        public static Row user(String name, String email, String role, Runnable click) {
            return new Row(true, name, email, role, null, null, null, null, null, click);
        }
    }

    private final List<Row> rows = new ArrayList<>();
    public void submit(List<Row> data) { rows.clear(); rows.addAll(data); notifyDataSetChanged(); }
    @Override public int getItemCount() { return rows.size(); }
    @Override public int getItemViewType(int position) { return rows.get(position).user ? 1 : 0; }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(
                type == 1 ? R.layout.item_user : R.layout.item_record, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Row row = rows.get(position);
        holder.title.setText(row.title); holder.details.setText(row.details); holder.badge.setText(row.badge);
        if (row.user) {
            StringBuilder initials = new StringBuilder();
            if (row.title != null) {
                for (String part : row.title.trim().split("\\s+")) {
                    if (!part.isEmpty() && initials.length() < 2) initials.append(part.substring(0, 1));
                }
            }
            holder.initials.setText(initials.toString().toUpperCase(Locale.ROOT));
        } else {
            holder.equipment.setText(row.equipment); holder.date.setText(row.date);
            holder.technician.setText(row.technician); holder.diagnosis.setText(row.diagnosis);
            holder.repair.setText(row.repair);
        }
        holder.itemView.setOnClickListener(row.click == null ? null : v -> row.click.run());
        holder.itemView.setFocusable(row.click != null);
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title, details, badge, initials, equipment, date, technician, diagnosis, repair;
        Holder(View view) {
            super(view);
            title = view.findViewById(R.id.recordTitle); details = view.findViewById(R.id.recordDetails);
            badge = view.findViewById(R.id.recordBadge); initials = view.findViewById(R.id.recordInitials);
            equipment = view.findViewById(R.id.recordEquipment); date = view.findViewById(R.id.recordDate);
            technician = view.findViewById(R.id.recordTechnician); diagnosis = view.findViewById(R.id.recordDiagnosis);
            repair = view.findViewById(R.id.recordRepair);
        }
    }
}
