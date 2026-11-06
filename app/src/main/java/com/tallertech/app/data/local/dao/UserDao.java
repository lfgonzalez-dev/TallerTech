package com.tallertech.app.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tallertech.app.data.local.entity.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Query("SELECT * FROM users ORDER BY name ASC")
    List<UserEntity> getAll();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity getById(String id);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getByEmail(String email);
}