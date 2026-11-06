package com.tallertech.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "equipment",
        foreignKeys = @ForeignKey(
                entity = ClientEntity.class,
                parentColumns = "id",
                childColumns = "clientId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("clientId"),
                @Index(value = "serial", unique = true)
        }
)
public class EquipmentEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String clientId;

    private String type;
    private String brand;
    private String model;
    private String serial;
    private String description;

    private long updatedAt;
    private String syncStatus;

    public EquipmentEntity(
            @NonNull String id,
            @NonNull String clientId,
            String type,
            String brand,
            String model,
            String serial,
            String description,
            long updatedAt,
            String syncStatus) {

        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.serial = serial;
        this.description = description;
        this.updatedAt = updatedAt;
        this.syncStatus = syncStatus;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getClientId() {
        return clientId;
    }

    public void setClientId(@NonNull String clientId) {
        this.clientId = clientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
}