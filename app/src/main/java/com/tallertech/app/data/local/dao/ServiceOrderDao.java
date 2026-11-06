package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tallertech.app.data.local.entity.ServiceOrderEntity;

import java.util.List;

@Dao
public interface ServiceOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ServiceOrderEntity order);

    @Update
    void update(ServiceOrderEntity order);

    @Delete
    void delete(ServiceOrderEntity order);

    @Query("SELECT * FROM service_orders ORDER BY entryDate DESC")
    List<ServiceOrderEntity> getAll();

    @Query("SELECT * FROM service_orders WHERE id = :id LIMIT 1")
    ServiceOrderEntity getById(String id);

    @Query("SELECT * FROM service_orders WHERE code = :code LIMIT 1")
    ServiceOrderEntity getByCode(String code);

    @Query("SELECT * FROM service_orders " +
            "WHERE status = :status " +
            "ORDER BY entryDate DESC")
    List<ServiceOrderEntity> getByStatus(String status);

    @Query("SELECT * FROM service_orders " +
            "WHERE clientId = :clientId " +
            "ORDER BY entryDate DESC")
    List<ServiceOrderEntity> getByClient(String clientId);

    @Query("SELECT * FROM service_orders " +
            "WHERE equipmentId = :equipmentId " +
            "ORDER BY entryDate DESC")
    List<ServiceOrderEntity> getByEquipment(String equipmentId);

    @Query("UPDATE service_orders " +
            "SET status = :status, " +
            "updatedAt = :updatedAt, " +
            "syncStatus = 'PENDING' " +
            "WHERE id = :orderId")
    void updateStatus(
            String orderId,
            String status,
            long updatedAt
    );

    @Query("SELECT COUNT(*) FROM service_orders")
    int count();
}