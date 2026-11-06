package com.tallertech.app.ui.orders;

public class OrderUiModel {

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