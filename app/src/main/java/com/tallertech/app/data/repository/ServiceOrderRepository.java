package com.tallertech.app.data.repository;

import android.content.Context;
import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.OrderDetails;
import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.local.entity.ServiceOrderEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ServiceOrderRepository {
    private final TallerTechDatabase database;

    public ServiceOrderRepository(Context context) {
        database = TallerTechDatabase.getInstance(context);
    }

    public void getAll(ResultCallback<List<OrderDetails>> callback) {
        RepositoryExecutor.run(() -> database.serviceOrderDao().getAllDetails(), callback);
    }

    public void getById(String id, ResultCallback<OrderDetails> callback) {
        RepositoryExecutor.run(() -> {
            OrderDetails details = database.serviceOrderDao().getDetails(id);
            if (details == null) throw new IllegalArgumentException("La orden no existe");
            return details;
        }, callback);
    }

    public void getLatestByClient(String id, ResultCallback<OrderDetails> callback) {
        RepositoryExecutor.run(() -> database.serviceOrderDao().getLatestByClient(id), callback);
    }

    public void create(String clientId, String equipmentId, String issue, long entryDate,
                       String technician, ResultCallback<String> callback) {
        RepositoryExecutor.run(() -> {
            String[] code = new String[1];
            database.runInTransaction(() -> {
                EquipmentEntity equipment = database.equipmentDao().getById(equipmentId);
                if (equipment == null || !equipment.getClientId().equals(clientId)) {
                    throw new IllegalArgumentException("El equipo no pertenece al cliente seleccionado");
                }
                // La transacción serializa la comprobación y la inserción local.
                do {
                    code[0] = "ORD-" + UUID.randomUUID().toString().replace("-", "")
                            .substring(0, 12).toUpperCase(Locale.ROOT);
                } while (database.serviceOrderDao().getByCode(code[0]) != null);
                database.serviceOrderDao().insert(new ServiceOrderEntity(UUID.randomUUID().toString(),
                        code[0], clientId, equipmentId, issue, entryDate, technician,
                        "Pendiente", System.currentTimeMillis(), "PENDING"));
            });
            return code[0];
        }, callback);
    }

    public void updateStatus(String id, String status, ResultCallback<Void> callback) {
        RepositoryExecutor.run(() -> {
            if (!Arrays.asList("Pendiente", "Diagnóstico", "En reparación", "Completado").contains(status)) {
                throw new IllegalArgumentException("Estado inválido");
            }
            database.runInTransaction(() -> {
                if (database.serviceOrderDao().getById(id) == null) {
                    throw new IllegalArgumentException("La orden no existe");
                }
                database.serviceOrderDao().updateStatus(id, status, System.currentTimeMillis());
            });
            return null;
        }, callback);
    }
}
