package com.tallertech.app.ui.common;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tallertech.app.R;
import java.util.*;

public final class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.Holder> {
    public static final class Row {
        final String title, details;
        final Runnable click;
        public Row(String title, String details, Runnable click) {
            this.title = title; this.details = details; this.click = click;
        }
    }
    private final List<Row> rows = new ArrayList<>();
    public void submit(List<Row> data) { rows.clear(); rows.addAll(data); notifyDataSetChanged(); }
    @Override public int getItemCount() { return rows.size(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Row row = rows.get(position);
        holder.title.setText(row.title); holder.details.setText(row.details);
        holder.itemView.setOnClickListener(row.click == null ? null : v -> row.click.run());
        holder.itemView.setFocusable(row.click != null);
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title, details;
        Holder(View view) { super(view); title = view.findViewById(R.id.recordTitle); details = view.findViewById(R.id.recordDetails); }
    }
}
