package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tallertech.app.data.local.entity.EquipmentEntity;

import java.util.List;

@Dao
public interface EquipmentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(EquipmentEntity equipment);

    @Update
    void update(EquipmentEntity equipment);

    @Delete
    void delete(EquipmentEntity equipment);

    @Query("SELECT * FROM equipment ORDER BY brand ASC, model ASC")
    List<EquipmentEntity> getAll();

    @Query("SELECT * FROM equipment WHERE id = :id LIMIT 1")
    EquipmentEntity getById(String id);

    @Query("SELECT * FROM equipment " +
            "WHERE clientId = :clientId " +
            "ORDER BY brand ASC, model ASC")
    List<EquipmentEntity> getByClient(String clientId);

    @Query("SELECT * FROM equipment WHERE serial = :serial LIMIT 1")
    EquipmentEntity getBySerial(String serial);

    @Query("SELECT COUNT(*) FROM equipment WHERE clientId = :clientId")
    int countByClientId(String clientId);

    @Query("SELECT COUNT(*) FROM equipment")
    int count();

    @Query("SELECT e.*, c.name AS clientName, COALESCE((SELECT o.status FROM service_orders o " +
            "WHERE o.equipmentId = e.id ORDER BY CASE WHEN o.status != 'Completado' THEN 0 ELSE 1 END, " +
            "o.entryDate DESC, o.rowid DESC LIMIT 1), 'Registrado') AS orderStatus " +
            "FROM equipment e JOIN clients c ON c.id = e.clientId ORDER BY e.brand ASC, e.model ASC")
    List<com.tallertech.app.data.local.EquipmentDetails> getAllDetails();
}
