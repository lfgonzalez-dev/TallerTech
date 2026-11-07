package com.tallertech.app.ui.equipment;

public class EquipmentUiModel {
    private final String id;
    private final String clientId;
    public String getId() { return id; }
    public String getClientId() { return clientId; }

    private final String type;
    private final String brand;
    private final String model;
    private final String serial;
    private final String description;
    private final String client;
    private final String status;

    // Conserva los IDs de Room para navegar sin depender del nombre.
    public EquipmentUiModel(
            String id,
            String clientId,
            String type,
            String brand,
            String model,
            String serial,
            String description,
            String client,
            String status) {

        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.serial = serial;
        this.description = description;
        this.client = client;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getSerial() {
        return serial;
    }

    public String getDescription() {
        return description;
    }

    public String getClient() {
        return client;
    }

    public String getStatus() {
        return status;
    }

    // Nombre que mostraremos como título del equipo.
    public String getFullName() {
        return brand + " " + model;
    }
}
