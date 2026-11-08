package com.tallertech.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "service_orders", uniqueConstraints = @UniqueConstraint(name = "uk_orders_code", columnNames = "code"))
public class ServiceOrder extends BaseEntity {
    @Column(name = "code", length = 255, nullable = false)
    private String code;

    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "equipment_id", nullable = false, length = 36)
    private String equipmentId;

    @Column(name = "issue", columnDefinition = "TEXT")
    private String issue;

    @Column(name = "entry_date", nullable = false)
    private Long entryDate;

    @Column(name = "technician", length = 255)
    private String technician;

    @Column(name = "status", length = 255, nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", insertable = false, updatable = false)
    private Equipment equipment;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }
    public Long getEntryDate() { return entryDate; }
    public void setEntryDate(Long entryDate) { this.entryDate = entryDate; }
    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

