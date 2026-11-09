package com.tallertech.app.data.sync;

import android.content.Context;
import android.net.*;
import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.repository.*;

public final class SyncRepository {
    public static final class State {
        public int pending, errors;
        public long lastSuccess;
        public boolean connected;
        public String problem;
    }
    private final Context context;
    private final TallerTechDatabase db;
    public SyncRepository(Context context) {
        this.context = context.getApplicationContext();
        db = TallerTechDatabase.getInstance(context);
    }
    static int count(TallerTechDatabase db, String status) {
        return db.clientDao().countSyncStatus(status) + db.equipmentDao().countSyncStatus(status)
                + db.serviceOrderDao().countSyncStatus(status) + db.diagnosisDao().countSyncStatus(status)
                + db.repairDao().countSyncStatus(status);
    }
    public void load(ResultCallback<State> callback) {
        RepositoryExecutor.run(() -> {
            State state = new State();
            db.runInTransaction(() -> {
                state.pending = count(db, SyncStatus.PENDING);
                state.errors = count(db, SyncStatus.ERROR);
            });
            ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            state.connected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            state.lastSuccess = SyncManager.preferences(context).getLong("lastSuccess", 0);
            state.problem = SyncManager.preferences(context).getString("problem", null);
            return state;
        }, callback);
    }
    public void synchronizeNow(ResultCallback<Void> callback) {
        RepositoryExecutor.run(() -> {
            db.runInTransaction(() -> {
                db.clientDao().retryErrors(); db.equipmentDao().retryErrors(); db.serviceOrderDao().retryErrors();
                db.diagnosisDao().retryErrors(); db.repairDao().retryErrors();
            });
            SyncManager.request(context);
            return null;
        }, callback);
    }
}
