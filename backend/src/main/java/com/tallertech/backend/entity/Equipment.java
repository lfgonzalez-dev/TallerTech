package com.tallertech.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment", uniqueConstraints = @UniqueConstraint(name = "uk_equipment_serial", columnNames = "serial"))
public class Equipment extends BaseEntity {
    @Column(name = "client_id", nullable = false, length = 36)
    private String clientId;

    @Column(name = "type", length = 255)
    private String type;

    @Column(name = "brand", length = 255)
    private String brand;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "serial", length = 255)
    private String serial;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

