package com.example.mshare.models;

import com.example.mshare.models.Data;

public class Sender {
    private Data data;
    private String to;

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }

    public Sender() {
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String toToken) {
        this.to = toToken;
    }
}
