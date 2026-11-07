package com.tallertech.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.dao.EquipmentDao;
import com.tallertech.app.data.local.entity.EquipmentEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class EquipmentRepository {

    private final EquipmentDao equipmentDao;
    private final TallerTechDatabase database;

    // Room no debe ejecutarse en el hilo principal.
    private final ExecutorService executor =
            RepositoryExecutor.IO;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public EquipmentRepository(Context context) {

        database =
                TallerTechDatabase.getInstance(context);

        equipmentDao = database.equipmentDao();
    }

    public void insert(
            EquipmentEntity equipment,
            OperationCallback callback) {

        executor.execute(() -> {

            try {

                equipmentDao.insert(equipment);

                mainHandler.post(() ->
                        callback.onSuccess()
                );

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onError(e)
                );
            }
        });
    }

    public void getAll(
            EquipmentCallback callback) {

        executor.execute(() -> {

            try {

                List<EquipmentEntity> equipment =
                        equipmentDao.getAll();

                mainHandler.post(() ->
                        callback.onResult(equipment)
                );

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onError(e)
                );
            }
        });
    }

    public interface OperationCallback {

        void onSuccess();

        void onError(Exception e);
    }

    public void getById(String id, ResultCallback<EquipmentEntity> callback) {
        RepositoryExecutor.run(() -> {
            EquipmentEntity equipment = equipmentDao.getById(id);
            if (equipment == null) throw new IllegalArgumentException("El equipo no existe");
            return equipment;
        }, callback);
    }

    public void getByClient(String id, ResultCallback<List<EquipmentEntity>> callback) {
        RepositoryExecutor.run(() -> equipmentDao.getByClient(id), callback);
    }

    public void getAllDetails(ResultCallback<List<com.tallertech.app.data.local.EquipmentDetails>> callback) {
        RepositoryExecutor.run(() -> equipmentDao.getAllDetails(), callback);
    }

    public void getStatus(String id, ResultCallback<String> callback) {
        RepositoryExecutor.run(() -> {
            String status = database.serviceOrderDao().getEquipmentStatus(id);
            return status == null ? "Registrado" : status;
        }, callback);
    }

    public void update(EquipmentEntity equipment, ResultCallback<Void> callback) {
        RepositoryExecutor.run(() -> {
            if (equipmentDao.getById(equipment.getId()) == null) {
                throw new IllegalArgumentException("El equipo no existe");
            }
            equipment.setUpdatedAt(System.currentTimeMillis());
            equipment.setSyncStatus("PENDING");
            equipmentDao.update(equipment);
            return null;
        }, callback);
    }

    public interface EquipmentCallback {

        void onResult(List<EquipmentEntity> equipment);

        void onError(Exception e);
    }
}
