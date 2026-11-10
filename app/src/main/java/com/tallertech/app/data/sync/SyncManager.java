package com.tallertech.app.data.sync;

import android.content.Context;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

public final class SyncManager {
    public static final String WORK_NAME = "tallertech-sync";
    private SyncManager() {}
    public static void request(Context context) {
        try {
            OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(SyncWorker.class)
                    .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS).build();
            // Serialize requests, including a write committed while the previous worker is finishing.
            WorkManager.getInstance(context.getApplicationContext())
                    .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, work);
        } catch (RuntimeException ex) {
            problem(context, "No se pudo programar la sincronización");
        }
    }
    public static android.content.SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences("sync_state", Context.MODE_PRIVATE);
    }
    public static void problem(Context context, String message) {
        preferences(context).edit().putString("problem", message).apply();
    }
}
