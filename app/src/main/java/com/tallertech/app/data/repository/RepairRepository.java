package com.tallertech.app.data.repository;

import android.content.Context;
import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.entity.RepairEntity;
import java.util.UUID;

public class RepairRepository {
    private final TallerTechDatabase database;

    public RepairRepository(Context context) {
        database = TallerTechDatabase.getInstance(context);
    }

    public void getByOrder(String orderId, ResultCallback<RepairEntity> callback) {
        RepositoryExecutor.run(() -> {
            if (database.serviceOrderDao().getById(orderId) == null) {
                throw new IllegalArgumentException("La orden no existe");
            }
            return database.repairDao().getByOrder(orderId);
        }, callback);
    }

    public void save(String orderId, String repair, String parts, String observations,
                     ResultCallback<Void> callback) {
        RepositoryExecutor.run(() -> {
            database.runInTransaction(() -> {
                RepairEntity existing = database.repairDao().getByOrder(orderId);
                RepairEntity entity = new RepairEntity(existing == null
                        ? UUID.randomUUID().toString() : existing.getId(), orderId,
                        repair, parts, observations, System.currentTimeMillis(), "PENDING");
                if (existing == null) database.repairDao().insert(entity);
                else database.repairDao().update(entity);
            });
            return null;
        }, callback);
    }
}
