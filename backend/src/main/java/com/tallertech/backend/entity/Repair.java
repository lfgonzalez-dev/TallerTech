package com.tallertech.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "repairs", uniqueConstraints = @UniqueConstraint(name = "uk_repairs_orderId", columnNames = "order_id"))
public class Repair extends BaseEntity {
    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "repair", columnDefinition = "TEXT")
    private String repair;

    @Column(name = "parts", columnDefinition = "TEXT")
    private String parts;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private ServiceOrder order;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getRepair() { return repair; }
    public void setRepair(String repair) { this.repair = repair; }
    public String getParts() { return parts; }
    public void setParts(String parts) { this.parts = parts; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}

