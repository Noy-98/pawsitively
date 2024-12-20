package com.example.pawsitively;

public class CommunityChatItem {
    private String senderId;
    private String username;
    private String message;
    private long timestamp;
    private String recipientContact;

    // No-argument constructor (required by Firebase)
    public CommunityChatItem() {
    }

    // Constructor without recipientContact
    public CommunityChatItem(String senderId, String username, String message, long timestamp) {
        this.senderId = senderId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Constructor with recipientContact
    public CommunityChatItem(String senderId, String username, String recipientContact, String message, long timestamp) {
        this.senderId = senderId;
        this.username = username;
        this.recipientContact = recipientContact;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRecipientContact() {
        return recipientContact;
    }

    public void setRecipientContact(String recipientContact) {
        this.recipientContact = recipientContact;
    }
}
