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
    @Query("SELECT o.id AS orderId, o.code, c.name AS clientName, e.brand || ' ' || e.model AS equipmentName, " +
            "o.entryDate, o.status, o.technician, d.diagnosis, r.repair FROM service_orders o " +
            "JOIN clients c ON c.id = o.clientId JOIN equipment e ON e.id = o.equipmentId " +
            "LEFT JOIN diagnoses d ON d.orderId = o.id LEFT JOIN repairs r ON r.orderId = o.id " +
            "ORDER BY o.entryDate DESC, o.id")
    java.util.List<com.tallertech.app.data.local.HistoryEntry> getHistory();

    @Insert(onConflict = OnConflictStrategy.ABORT)
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

    @Query("SELECT COUNT(*) FROM service_orders WHERE entryDate >= :since")
    int countRecent(long since);

    @Query("SELECT o.*, c.name AS clientName, e.brand || ' ' || e.model AS equipmentName " +
            "FROM service_orders o JOIN clients c ON c.id = o.clientId " +
            "JOIN equipment e ON e.id = o.equipmentId ORDER BY o.entryDate DESC, o.rowid DESC")
    List<com.tallertech.app.data.local.OrderDetails> getAllDetails();

    @Query("SELECT o.*, c.name AS clientName, e.brand || ' ' || e.model AS equipmentName " +
            "FROM service_orders o JOIN clients c ON c.id = o.clientId " +
            "JOIN equipment e ON e.id = o.equipmentId WHERE o.id = :id")
    com.tallertech.app.data.local.OrderDetails getDetails(String id);

    @Query("SELECT o.*, c.name AS clientName, e.brand || ' ' || e.model AS equipmentName " +
            "FROM service_orders o JOIN clients c ON c.id = o.clientId " +
            "JOIN equipment e ON e.id = o.equipmentId " +
            "ORDER BY o.entryDate DESC, o.rowid DESC LIMIT 1")
    com.tallertech.app.data.local.OrderDetails getLatestDetails();

    @Query("SELECT o.*, c.name AS clientName, e.brand || ' ' || e.model AS equipmentName " +
            "FROM service_orders o JOIN clients c ON c.id = o.clientId " +
            "JOIN equipment e ON e.id = o.equipmentId WHERE o.clientId = :clientId " +
            "ORDER BY o.entryDate DESC, o.rowid DESC LIMIT 1")
    com.tallertech.app.data.local.OrderDetails getLatestByClient(String clientId);

    @Query("SELECT COUNT(*) FROM service_orders WHERE status != 'Completado'")
    int countActive();

    @Query("SELECT COUNT(DISTINCT equipmentId) FROM service_orders WHERE status != 'Completado'")
    int countActiveEquipment();

    @Query("SELECT COUNT(DISTINCT equipmentId) FROM service_orders WHERE status = 'En reparación'")
    int countRepairEquipment();

    // Un equipo está listo si tiene servicios completados y ninguno activo.
    @Query("SELECT COUNT(DISTINCT o.equipmentId) FROM service_orders o " +
            "WHERE o.status = 'Completado' AND NOT EXISTS " +
            "(SELECT 1 FROM service_orders a WHERE a.equipmentId = o.equipmentId AND a.status != 'Completado')")
    int countReadyEquipment();

    @Query("SELECT status FROM service_orders WHERE equipmentId = :equipmentId " +
            "ORDER BY CASE WHEN status != 'Completado' THEN 0 ELSE 1 END, entryDate DESC, rowid DESC LIMIT 1")
    String getEquipmentStatus(String equipmentId);
    @Query("SELECT * FROM service_orders WHERE syncStatus = 'PENDING'")
    java.util.List<ServiceOrderEntity> getPending();

    @Query("SELECT COUNT(*) FROM service_orders WHERE syncStatus = :status")
    int countSyncStatus(String status);

    @Query("UPDATE service_orders SET syncStatus = :status WHERE id = :id AND updatedAt = :version")
    void setSyncStatus(String id, long version, String status);

    @Query("UPDATE service_orders SET syncStatus = 'PENDING' WHERE syncStatus = 'ERROR'")
    void retryErrors();
}
