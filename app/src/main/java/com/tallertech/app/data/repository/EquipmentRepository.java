package com.tallertech.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.dao.EquipmentDao;
import com.tallertech.app.data.local.entity.EquipmentEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EquipmentRepository {

    private final EquipmentDao equipmentDao;

    // Room no debe ejecutarse en el hilo principal.
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public EquipmentRepository(Context context) {

        TallerTechDatabase database =
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

    public interface EquipmentCallback {

        void onResult(List<EquipmentEntity> equipment);

        void onError(Exception e);
    }
}
