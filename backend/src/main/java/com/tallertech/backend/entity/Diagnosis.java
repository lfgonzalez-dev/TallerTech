package com.tallertech.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "diagnoses", uniqueConstraints = @UniqueConstraint(name = "uk_diagnoses_orderId", columnNames = "order_id"))
public class Diagnosis extends BaseEntity {
    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private ServiceOrder order;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
}

