package com.tallertech.app.data.repository;

import android.content.Context;
import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.OrderDetails;

public class StatisticsRepository {
    public static class Statistics {
        public int clients, equipment, activeOrders, activeEquipment, repairEquipment, readyEquipment;
        public OrderDetails latestOrder;
    }

    private final TallerTechDatabase database;

    public StatisticsRepository(Context context) {
        database = TallerTechDatabase.getInstance(context);
    }

    public void load(ResultCallback<Statistics> callback) {
        RepositoryExecutor.run(() -> {
            Statistics stats = new Statistics();
            database.runInTransaction(() -> {
                stats.clients = database.clientDao().count();
                stats.equipment = database.equipmentDao().count();
                stats.activeOrders = database.serviceOrderDao().countActive();
                stats.activeEquipment = database.serviceOrderDao().countActiveEquipment();
                stats.repairEquipment = database.serviceOrderDao().countRepairEquipment();
                stats.readyEquipment = database.serviceOrderDao().countReadyEquipment();
                stats.latestOrder = database.serviceOrderDao().getLatestDetails();
            });
            return stats;
        }, callback);
    }
}
