package com.example.pawsitively;

public class MessageItem {
    private String senderId;
    private String message;
    private long timestamp; // Add a timestamp field

    public MessageItem() { } // Default constructor for Firebase

    public MessageItem(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp; // Set timestamp
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp; // Getter for timestamp
    }
}
