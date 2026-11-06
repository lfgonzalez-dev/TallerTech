package com.tallertech.app.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tallertech.app.data.local.dao.ClientDao;
import com.tallertech.app.data.local.dao.DiagnosisDao;
import com.tallertech.app.data.local.dao.EquipmentDao;
import com.tallertech.app.data.local.dao.RepairDao;
import com.tallertech.app.data.local.dao.ServiceOrderDao;
import com.tallertech.app.data.local.dao.UserDao;
import com.tallertech.app.data.local.entity.ClientEntity;
import com.tallertech.app.data.local.entity.DiagnosisEntity;
import com.tallertech.app.data.local.entity.EquipmentEntity;
import com.tallertech.app.data.local.entity.RepairEntity;
import com.tallertech.app.data.local.entity.ServiceOrderEntity;
import com.tallertech.app.data.local.entity.UserEntity;

@Database(
        entities = {
                ClientEntity.class,
                EquipmentEntity.class,
                ServiceOrderEntity.class,
                DiagnosisEntity.class,
                RepairEntity.class,
                UserEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class TallerTechDatabase
        extends RoomDatabase {

    private static volatile TallerTechDatabase INSTANCE;

    public abstract ClientDao clientDao();

    public abstract EquipmentDao equipmentDao();

    public abstract ServiceOrderDao serviceOrderDao();

    public abstract DiagnosisDao diagnosisDao();

    public abstract RepairDao repairDao();

    public abstract UserDao userDao();

    public static TallerTechDatabase getInstance(
            Context context) {

        if (INSTANCE == null) {

            synchronized (TallerTechDatabase.class) {

                if (INSTANCE == null) {

                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TallerTechDatabase.class,
                                    "tallertech_database"
                            )
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}