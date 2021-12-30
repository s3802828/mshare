package com.example.mshare.models;

public class NotificationResponse {
    private int success;

    public NotificationResponse(int success) {
        this.success = success;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public NotificationResponse() {
    }
}
