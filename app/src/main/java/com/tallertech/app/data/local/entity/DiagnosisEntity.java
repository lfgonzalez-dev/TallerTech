package com.tallertech.app.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "diagnoses",
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
public class DiagnosisEntity {

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String orderId;

    private String diagnosis;
    private String observations;
    private String recommendations;

    private long updatedAt;
    private String syncStatus;

    public DiagnosisEntity(
            @NonNull String id,
            @NonNull String orderId,
            String diagnosis,
            String observations,
            String recommendations,
            long updatedAt,
            String syncStatus) {

        this.id = id;
        this.orderId = orderId;
        this.diagnosis = diagnosis;
        this.observations = observations;
        this.recommendations = recommendations;
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

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
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