package com.tallertech.app.ui.clients;

public class ClientUiModel {
    private final String id;
    public String getId() { return id; }

    private final String initials;
    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String notes;
    private final String equipment;

    // Conserva el identificador local para detalle y edición.
    public ClientUiModel(
            String id,
            String initials,
            String name,
            String phone,
            String email,
            String address,
            String notes,
            String equipment) {

        this.id = id;
        this.initials = initials;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.notes = notes;
        this.equipment = equipment;
    }

    public String getInitials() {
        return initials;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getNotes() {
        return notes;
    }

    public String getEquipment() {
        return equipment;
    }

    // Se utiliza para mostrar teléfono y correo juntos en el RecyclerView.
    public String getContact() {
        return phone + " · " + email;
    }
}
