package com.tallertech.app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.dao.ClientDao;
import com.tallertech.app.data.local.entity.ClientEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRepository {

    private final ClientDao clientDao;

    // Room no debe ejecutarse en el hilo principal.
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public ClientRepository(Context context) {

        TallerTechDatabase database =
                TallerTechDatabase.getInstance(context);

        clientDao = database.clientDao();
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

    public interface OperationCallback {

        void onSuccess();

        void onError(Exception e);
    }

    public interface ClientsCallback {

        void onResult(List<ClientEntity> clients);

        void onError(Exception e);
    }
}