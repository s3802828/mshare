package com.example.mshare.models;

import java.util.Date;

public class Message {
    public String senderId, receiverId, content, timestamp;

    public String conversationName, conversationAvatar;

    public Date date;

    public Message() {
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return timestamp;
    }

    public String getConversationName() { return conversationName; }

    public String getConversationAvatar() { return conversationAvatar; }


}
