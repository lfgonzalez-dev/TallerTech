package com.tallertech.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.dao.ClientDao;
import com.tallertech.app.data.local.entity.ClientEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.tallertech.app.data.local.dao.EquipmentDao;
import java.util.concurrent.ExecutorService;

public class ClientRepository {

    private final ClientDao clientDao;
    private final EquipmentDao equipmentDao;

    // Room no debe ejecutarse en el hilo principal.
    private final ExecutorService executor =
            RepositoryExecutor.IO;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public ClientRepository(Context context) {

        TallerTechDatabase database =
                TallerTechDatabase.getInstance(context);

        clientDao = database.clientDao();
        equipmentDao = database.equipmentDao();
    }

    public void insert(
            ClientEntity client,
            OperationCallback callback) {

        executor.execute(() -> {

            try {

                clientDao.insert(client);

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
            ClientsCallback callback) {

        executor.execute(() -> {

            try {

                List<ClientEntity> clients =
                        clientDao.getAll();

                mainHandler.post(() ->
                        callback.onResult(clients)
                );

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onError(e)
                );
            }
        });
    }

    public void getAllWithEquipmentCounts(ClientsWithCountsCallback callback) {
        executor.execute(() -> {
            try {
                List<ClientEntity> clients = clientDao.getAll();
                Map<String, Integer> counts = new HashMap<>();
                for (ClientEntity client : clients) {
                    counts.put(client.getId(), equipmentDao.countByClientId(client.getId()));
                }
                mainHandler.post(() -> callback.onResult(clients, counts));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    public interface ClientsWithCountsCallback {
        void onResult(List<ClientEntity> clients, Map<String, Integer> counts);
        void onError(Exception e);
    }

    public void getById(String id, ResultCallback<ClientEntity> callback) {
        RepositoryExecutor.run(() -> {
            ClientEntity client = clientDao.getById(id);
            if (client == null) throw new IllegalArgumentException("El cliente no existe");
            return client;
        }, callback);
    }

    public void countEquipment(String id, ResultCallback<Integer> callback) {
        RepositoryExecutor.run(() -> equipmentDao.countByClientId(id), callback);
    }

    public void update(ClientEntity client, ResultCallback<Void> callback) {
        RepositoryExecutor.run(() -> {
            if (clientDao.getById(client.getId()) == null) {
                throw new IllegalArgumentException("El cliente no existe");
            }
            client.setUpdatedAt(System.currentTimeMillis());
            client.setSyncStatus("PENDING");
            clientDao.update(client);
            return null;
        }, callback);
    }

    public interface OperationCallback {

        void onSuccess();

        void onError(Exception e);
    }

    public interface ClientsCallback {

        void onResult(List<ClientEntity> clients);

        void onError(Exception e);
    }
}
