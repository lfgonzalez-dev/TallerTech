package com.tallertech.app.data.sync;

import android.content.Context;
import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.remote.RetrofitClient;
import com.tallertech.app.data.remote.session.TokenManager;
import java.io.IOException;
import java.util.function.BooleanSupplier;

public final class SyncCoordinator {
    private final TallerTechDatabase db;
    private final Context context;
    private final BooleanSupplier stopped;
    public SyncCoordinator(Context context, BooleanSupplier stopped) {
        this.context = context.getApplicationContext();
        this.db = TallerTechDatabase.getInstance(context);
        this.stopped = stopped;
    }
    public boolean synchronize() throws IOException {
        return new SyncEngine(SyncResources.ordered(db, RetrofitClient.getApi(context)),
                db::runInTransaction, this::checkSession, message -> {
                    android.util.Log.w("TallerTechSync", message);
                    SyncManager.problem(context, message.replaceAll(" [0-9a-fA-F-]{36}", ""));
                }).synchronize();
    }
    private void checkSession() throws IOException {
        if (stopped.getAsBoolean()) throw new IOException("Trabajo interrumpido");
        if (!new TokenManager(context).hasToken()) throw new SyncHttp.Failure(401);
    }
}
