package com.tallertech.app.ui.orders;

public class OrderUiModel {
    private String id;
    public String getId() { return id; }

    public static OrderUiModel fromDetails(com.tallertech.app.data.local.OrderDetails details) {
        com.tallertech.app.data.local.entity.ServiceOrderEntity order = details.order;
        OrderUiModel model = new OrderUiModel(order.getCode(), details.clientName, details.equipmentName,
                order.getIssue(), new java.text.SimpleDateFormat("dd MMM yyyy", new java.util.Locale("es", "SV"))
                .format(new java.util.Date(order.getEntryDate())), order.getTechnician(), order.getStatus());
        model.id = order.getId();
        return model;
    }

    private final String code;
    private final String client;
    private final String equipment;
    private final String issue;
    private final String date;
    private final String technician;
    private final String status;

    public OrderUiModel(
            String code,
            String client,
            String equipment,
            String issue,
            String date,
            String technician,
            String status) {

        this.code = code;
        this.client = client;
        this.equipment = equipment;
        this.issue = issue;
        this.date = date;
        this.technician = technician;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getClient() {
        return client;
    }

    public String getEquipment() {
        return equipment;
    }

    public String getIssue() {
        return issue;
    }

    public String getDate() {
        return date;
    }

    public String getTechnician() {
        return technician;
    }

    public String getStatus() {
        return status;
    }
}
