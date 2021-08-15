package com.example.sisca_app.Models;

public class UserStatusModel {

    String id,status;

    public UserStatusModel(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public UserStatusModel() {
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
