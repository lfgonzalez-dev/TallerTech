package com.tallertech.app.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.tallertech.app.R;
import com.tallertech.app.data.local.*;
import com.tallertech.app.data.repository.*;
import com.tallertech.app.ui.common.RecordAdapter;
import com.tallertech.app.ui.orders.*;
import java.text.DateFormat;
import java.util.*;

public final class HistoryFragment extends Fragment {
    private RecordAdapter adapter;
    public HistoryFragment() { super(R.layout.fragment_records); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        ((TextView) view.findViewById(R.id.recordsTitle)).setText("Historial");
        view.findViewById(R.id.btnRecordsAction).setVisibility(View.GONE);
        view.findViewById(R.id.btnRecordsBack).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        adapter = new RecordAdapter();
        RecyclerView list = view.findViewById(R.id.recordsList);
        list.setLayoutManager(new LinearLayoutManager(requireContext())); list.setAdapter(adapter);
        LocalDataObserver.observe(this, this::load);
    }
    private static String text(String value) { return value == null || value.isBlank() ? "Sin registrar" : value; }
    private void load() {
        View view = getView();
        if (view == null) return;
        new HistoryRepository(requireContext()).load(new ResultCallback<List<HistoryEntry>>() {
            @Override public void onResult(List<HistoryEntry> entries) {
                if (getView() != view) return;
                List<RecordAdapter.Row> rows = new ArrayList<>();
                for (HistoryEntry entry : entries) rows.add(RecordAdapter.Row.history(
                        entry.code, entry.status, entry.equipmentName, entry.clientName,
                        DateFormat.getDateInstance().format(new Date(entry.entryDate)), text(entry.technician),
                        text(entry.diagnosis), text(entry.repair), () -> open(entry.orderId)));
                adapter.submit(rows);
                ((TextView) view.findViewById(R.id.recordsStatus)).setText(entries.isEmpty() ? "No hay órdenes registradas" : entries.size() + " órdenes");
            }
            @Override public void onError(Exception error) {
                if (getView() == view) ((TextView) view.findViewById(R.id.recordsStatus)).setText("No se pudo cargar el historial local");
            }
        });
    }
    private void open(String id) {
        Fragment detail = new OrderDetailFragment(); Bundle args = new Bundle(); args.putString("id", id); detail.setArguments(args);
        getParentFragmentManager().beginTransaction().replace(R.id.fragmentContainer, detail).addToBackStack(null).commit();
    }
}
