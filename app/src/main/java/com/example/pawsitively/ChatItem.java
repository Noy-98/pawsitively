package com.example.pawsitively;
public class ChatItem {
    private String userId;
    private String username;
    private String profileImageUrl;
    private long lastMessageTimestamp;

    public ChatItem(String userId, String username, String profileImageUrl, long lastMessageTimestamp) {
        this.userId = userId;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }
}
