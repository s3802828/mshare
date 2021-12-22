package com.example.mshare.model;

import java.util.Date;

public class Message {
    public String senderId, receiverId, content, timestamp;
    public Date date;

    public Message(){}

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }


}
