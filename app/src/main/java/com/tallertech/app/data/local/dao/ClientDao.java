package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tallertech.app.data.local.entity.ClientEntity;

import java.util.List;

@Dao
public interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ClientEntity client);

    @Update
    void update(ClientEntity client);

    @Delete
    void delete(ClientEntity client);

    @Query("SELECT * FROM clients ORDER BY name ASC")
    List<ClientEntity> getAll();

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    ClientEntity getById(String id);

    @Query("SELECT * FROM clients " +
            "WHERE name LIKE '%' || :query || '%' " +
            "OR phone LIKE '%' || :query || '%' " +
            "OR email LIKE '%' || :query || '%' " +
            "ORDER BY name ASC")
    List<ClientEntity> search(String query);

    @Query("SELECT COUNT(*) FROM clients")
    int count();
}