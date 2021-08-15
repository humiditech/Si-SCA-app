package com.example.sisca_app.Models;

public class DoctorStatusModel {
    String id,status;

    public DoctorStatusModel(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public DoctorStatusModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
