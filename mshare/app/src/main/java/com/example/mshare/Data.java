package com.example.mshare;

public class Data {
    private String senderId;
    private String senderAvatar;
    private String body;
    private String title;
    private String receiverId;

    public Data(String senderId, String senderAvatar, String body, String title, String receiverId) {
        this.senderId = senderId;
        this.senderAvatar = senderAvatar;
        this.body = body;
        this.title = title;
        this.receiverId = receiverId;
    }

    public Data() {
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
}
