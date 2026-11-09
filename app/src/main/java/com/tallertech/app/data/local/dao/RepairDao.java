package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tallertech.app.data.local.entity.RepairEntity;

@Dao
public interface RepairDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(RepairEntity repair);

    @Update
    void update(RepairEntity repair);

    @Query("SELECT * FROM repairs WHERE id = :id LIMIT 1")
    RepairEntity getById(String id);

    @Query("SELECT * FROM repairs WHERE orderId = :orderId LIMIT 1")
    RepairEntity getByOrder(String orderId);

    @Query("DELETE FROM repairs WHERE id = :id")
    void deleteById(String id);
    @Query("SELECT * FROM repairs WHERE syncStatus = 'PENDING'")
    java.util.List<RepairEntity> getPending();

    @Query("SELECT COUNT(*) FROM repairs WHERE syncStatus = :status")
    int countSyncStatus(String status);

    @Query("UPDATE repairs SET syncStatus = :status WHERE id = :id AND updatedAt = :version")
    void setSyncStatus(String id, long version, String status);

    @Query("UPDATE repairs SET syncStatus = 'PENDING' WHERE syncStatus = 'ERROR'")
    void retryErrors();
}