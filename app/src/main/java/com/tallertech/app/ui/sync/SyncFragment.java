package com.tallertech.app.ui.sync;

import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.work.*;
import com.tallertech.app.R;
import com.tallertech.app.data.sync.*;
import com.tallertech.app.data.local.LocalDataObserver;
import com.tallertech.app.data.repository.ResultCallback;
import java.text.DateFormat;
import java.util.Date;

public final class SyncFragment extends Fragment {
    private boolean running, requesting, queued;
    private ConnectivityManager connectivity;
    private final ConnectivityManager.NetworkCallback network = new ConnectivityManager.NetworkCallback() {
        @Override public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities caps) { refreshOnMain(); }
        @Override public void onLost(@NonNull Network network) { refreshOnMain(); }
    };
    public SyncFragment() { super(R.layout.fragment_sync); }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        view.findViewById(R.id.btnBackSync).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.btnSyncNow).setOnClickListener(v -> {
            requesting = true; v.setEnabled(false);
            new SyncRepository(requireContext()).synchronizeNow(new ResultCallback<Void>() {
                @Override public void onResult(Void ignored) { requesting = false; refresh(); }
                @Override public void onError(Exception error) { requesting = false; refresh(); }
            });
        });
        LocalDataObserver.observe(this, this::refresh);
        WorkManager.getInstance(requireContext()).getWorkInfosForUniqueWorkLiveData(SyncManager.WORK_NAME)
                .observe(getViewLifecycleOwner(), infos -> {
                    running = false; queued = false;
                    for (WorkInfo info : infos) {
                        running |= info.getState() == WorkInfo.State.RUNNING;
                        queued |= info.getState() == WorkInfo.State.ENQUEUED || info.getState() == WorkInfo.State.BLOCKED;
                    }
                    refresh();
                });
    }
    @Override public void onStart() {
        super.onStart();
        connectivity = requireContext().getSystemService(ConnectivityManager.class);
        connectivity.registerDefaultNetworkCallback(network);
        refresh();
    }
    @Override public void onStop() { connectivity.unregisterNetworkCallback(network); super.onStop(); }
    private void refreshOnMain() { if (getActivity() != null) requireActivity().runOnUiThread(this::refresh); }
    private void refresh() {
        View view = getView();
        if (view == null || !isAdded()) return;
        new SyncRepository(requireContext()).load(new ResultCallback<SyncRepository.State>() {
            @Override public void onResult(SyncRepository.State state) {
                if (getView() != view) return;
                String status = running ? "Sincronizando" : state.errors > 0 || state.problem != null ? "Error"
                        : state.pending > 0 || queued ? "Pendiente" : state.lastSuccess > 0 ? "Sincronizado" : "Pendiente";
                ((TextView) view.findViewById(R.id.syncGeneral)).setText(status);
                ((TextView) view.findViewById(R.id.syncConnectivity)).setText(state.connected ? "Conexión disponible" : "Sin conexión a Internet");
                ((TextView) view.findViewById(R.id.syncCounts)).setText("Pendientes: " + state.pending + "\nCon error: " + state.errors);
                ((TextView) view.findViewById(R.id.syncLastSuccess)).setText("Última sincronización exitosa: " +
                        (state.lastSuccess == 0 ? "Todavía no realizada" : DateFormat.getDateTimeInstance().format(new Date(state.lastSuccess))));
                ((TextView) view.findViewById(R.id.syncProblem)).setText(state.problem == null ? "" : state.problem);
                view.findViewById(R.id.btnSyncNow).setEnabled(!running && !requesting);
            }
            @Override public void onError(Exception error) {
                if (getView() == view) ((TextView) view.findViewById(R.id.syncGeneral)).setText("Error al consultar el estado local");
            }
        });
    }
}
