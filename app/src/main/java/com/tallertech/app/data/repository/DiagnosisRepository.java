package com.tallertech.app.data.repository;

import android.content.Context;
import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.entity.DiagnosisEntity;
import java.util.UUID;

public class DiagnosisRepository {
    private final Context syncContext;
    private final TallerTechDatabase database;

    public DiagnosisRepository(Context context) {
        syncContext = context.getApplicationContext();
        database = TallerTechDatabase.getInstance(context);
    }

    public void getByOrder(String orderId, ResultCallback<DiagnosisEntity> callback) {
        RepositoryExecutor.run(() -> {
            if (database.serviceOrderDao().getById(orderId) == null) {
                throw new IllegalArgumentException("La orden no existe");
            }
            return database.diagnosisDao().getByOrder(orderId);
        }, callback);
    }

    public void save(String orderId, String diagnosis, String observations,
                     String recommendations, ResultCallback<Void> callback) {
        RepositoryExecutor.run(() -> {
            database.runInTransaction(() -> {
                DiagnosisEntity existing = database.diagnosisDao().getByOrder(orderId);
                DiagnosisEntity entity = new DiagnosisEntity(existing == null
                        ? UUID.randomUUID().toString() : existing.getId(), orderId,
                        diagnosis, observations, recommendations, com.tallertech.app.data.sync.LastWriteWins.nextLocalTimestamp(existing == null ? 0 : existing.getUpdatedAt()), "PENDING");
                if (existing == null) database.diagnosisDao().insert(entity);
                else database.diagnosisDao().update(entity);
            });
            com.tallertech.app.data.sync.SyncManager.request(syncContext);
            return null;
        }, callback);
    }
}
