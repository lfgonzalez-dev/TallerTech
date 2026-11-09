package com.tallertech.app.data.sync;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import java.io.IOException;

public final class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters parameters) { super(context, parameters); }
    @NonNull @Override public Result doWork() {
        Context context = getApplicationContext();
        SyncManager.preferences(context).edit().remove("problem").apply();
        try {
            boolean complete = new SyncCoordinator(context, this::isStopped).synchronize();
            var db = com.tallertech.app.data.local.TallerTechDatabase.getInstance(context);
            if (complete && SyncRepository.count(db, SyncStatus.PENDING) == 0 && SyncRepository.count(db, SyncStatus.ERROR) == 0)
                SyncManager.preferences(context).edit().putLong("lastSuccess", System.currentTimeMillis()).apply();
            return Result.success();
        } catch (SyncHttp.Failure ex) {
            if (ex.code == 401) {
                com.tallertech.app.data.repository.AuthRepository.invalidateSession();
                SyncManager.problem(context, "Sesión expirada. Inicia sesión para sincronizar.");
                return Result.failure();
            }
            SyncManager.problem(context, ex.code == 403 ? "Sin permiso para sincronizar (403)" : "Servidor: HTTP " + ex.code);
            return ex.retryable() && getRunAttemptCount() < 5 ? Result.retry() : Result.failure();
        } catch (IOException ex) {
            SyncManager.problem(context, "No se pudo conectar. Los cambios permanecen guardados.");
            return getRunAttemptCount() < 5 ? Result.retry() : Result.failure();
        } catch (RuntimeException ex) {
            SyncManager.problem(context, "No se pudo completar el merge local. Revisa los datos.");
            return Result.failure();
        }
    }
}
