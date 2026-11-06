package com.tallertech.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = "email", unique = true)
        }
)
public class UserEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String email;
    private String role;
    private long updatedAt;

    public UserEntity(
            @NonNull String id,
            String name,
            String email,
            String role,
            long updatedAt) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}