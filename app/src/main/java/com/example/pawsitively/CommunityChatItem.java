package com.example.pawsitively;

public class CommunityChatItem {
    private String senderId;
    private String username;
    private String message;
    private long timestamp;

    public CommunityChatItem(String senderId, String username, String message, long timestamp) {
        this.senderId = senderId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
