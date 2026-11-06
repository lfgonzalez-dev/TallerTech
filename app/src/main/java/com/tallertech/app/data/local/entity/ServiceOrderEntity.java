package com.tallertech.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "service_orders",
        foreignKeys = {
                @ForeignKey(
                        entity = ClientEntity.class,
                        parentColumns = "id",
                        childColumns = "clientId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = EquipmentEntity.class,
                        parentColumns = "id",
                        childColumns = "equipmentId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("clientId"),
                @Index("equipmentId"),
                @Index(value = "code", unique = true)
        }
)
public class ServiceOrderEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String code;

    @NonNull
    private String clientId;

    @NonNull
    private String equipmentId;

    private String issue;
    private long entryDate;
    private String technician;
    private String status;

    private long updatedAt;
    private String syncStatus;

    public ServiceOrderEntity(
            @NonNull String id,
            String code,
            @NonNull String clientId,
            @NonNull String equipmentId,
            String issue,
            long entryDate,
            String technician,
            String status,
            long updatedAt,
            String syncStatus) {

        this.id = id;
        this.code = code;
        this.clientId = clientId;
        this.equipmentId = equipmentId;
        this.issue = issue;
        this.entryDate = entryDate;
        this.technician = technician;
        this.status = status;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @NonNull
    public String getClientId() {
        return clientId;
    }

    public void setClientId(@NonNull String clientId) {
        this.clientId = clientId;
    }

    @NonNull
    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(@NonNull String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public long getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(long entryDate) {
        this.entryDate = entryDate;
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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