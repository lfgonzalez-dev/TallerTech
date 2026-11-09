package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tallertech.app.data.local.entity.DiagnosisEntity;

@Dao
public interface DiagnosisDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(DiagnosisEntity diagnosis);

    @Update
    void update(DiagnosisEntity diagnosis);

    @Query("SELECT * FROM diagnoses WHERE id = :id LIMIT 1")
    DiagnosisEntity getById(String id);

    @Query("SELECT * FROM diagnoses WHERE orderId = :orderId LIMIT 1")
    DiagnosisEntity getByOrder(String orderId);

    @Query("DELETE FROM diagnoses WHERE id = :id")
    void deleteById(String id);
    @Query("SELECT * FROM diagnoses WHERE syncStatus = 'PENDING'")
    java.util.List<DiagnosisEntity> getPending();

    @Query("SELECT COUNT(*) FROM diagnoses WHERE syncStatus = :status")
    int countSyncStatus(String status);

    @Query("UPDATE diagnoses SET syncStatus = :status WHERE id = :id AND updatedAt = :version")
    void setSyncStatus(String id, long version, String status);

    @Query("UPDATE diagnoses SET syncStatus = 'PENDING' WHERE syncStatus = 'ERROR'")
    void retryErrors();
}