package com.tallertech.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "repairs",
        foreignKeys = @ForeignKey(
                entity = ServiceOrderEntity.class,
                parentColumns = "id",
                childColumns = "orderId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "orderId", unique = true)
        }
)
public class RepairEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String orderId;

    private String repair;
    private String parts;
    private String observations;

    private long updatedAt;
    private String syncStatus;

    public RepairEntity(
            @NonNull String id,
            @NonNull String orderId,
            String repair,
            String parts,
            String observations,
            long updatedAt,
            String syncStatus) {

        this.id = id;
        this.orderId = orderId;
        this.repair = repair;
        this.parts = parts;
        this.observations = observations;
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
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(@NonNull String orderId) {
        this.orderId = orderId;
    }

    public String getRepair() {
        return repair;
    }

    public void setRepair(String repair) {
        this.repair = repair;
    }

    public String getParts() {
        return parts;
    }

    public void setParts(String parts) {
        this.parts = parts;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
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