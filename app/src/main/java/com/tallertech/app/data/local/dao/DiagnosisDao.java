package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tallertech.app.data.local.entity.DiagnosisEntity;

@Dao
public interface DiagnosisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DiagnosisEntity diagnosis);

    @Update
    void update(DiagnosisEntity diagnosis);

    @Query("SELECT * FROM diagnoses WHERE id = :id LIMIT 1")
    DiagnosisEntity getById(String id);

    @Query("SELECT * FROM diagnoses WHERE orderId = :orderId LIMIT 1")
    DiagnosisEntity getByOrder(String orderId);

    @Query("DELETE FROM diagnoses WHERE id = :id")
    void deleteById(String id);
}