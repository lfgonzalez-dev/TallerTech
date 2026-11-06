package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tallertech.app.data.local.entity.RepairEntity;

@Dao
public interface RepairDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RepairEntity repair);

    @Update
    void update(RepairEntity repair);

    @Query("SELECT * FROM repairs WHERE id = :id LIMIT 1")
    RepairEntity getById(String id);

    @Query("SELECT * FROM repairs WHERE orderId = :orderId LIMIT 1")
    RepairEntity getByOrder(String orderId);

    @Query("DELETE FROM repairs WHERE id = :id")
    void deleteById(String id);
}