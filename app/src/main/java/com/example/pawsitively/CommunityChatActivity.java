package com.example.pawsitively;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommunityChatActivity extends AppCompatActivity {
    private ListView communityChatListView;
    private CommunityChatAdapter communityChatAdapter;
    private List<CommunityChatItem> communityChats;
    private DatabaseReference communityChatRef;
    private DatabaseReference userRef;
    private EditText messageInput;
    private Button sendButton;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_chat);

        communityChatListView = findViewById(R.id.communityChatListView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        communityChats = new ArrayList<>();
        communityChatAdapter = new CommunityChatAdapter(this, communityChats);
        communityChatListView.setAdapter(communityChatAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        communityChatRef = FirebaseDatabase.getInstance().getReference().child("community_chats");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        loadCommunityMessages();

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadCommunityMessages() {
        communityChatRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CommunityChatItem> newMessages = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String senderId = messageSnapshot.child("senderId").getValue(String.class);
                    String message = messageSnapshot.child("message").getValue(String.class);
                    Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                    if (senderId != null && message != null && timestamp != null) {
                        // Fetch the username for the senderId
                        userRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String username = userSnapshot.child("name").getValue(String.class);
                                if (username != null) {
                                    // Add message with the username to the list
                                    CommunityChatItem chatItem = new CommunityChatItem(senderId, username, message, timestamp);
                                    newMessages.add(chatItem);

                                    // Refresh the adapter after fetching all data
                                    communityChatAdapter.refreshData(newMessages);
                                    communityChatListView.setSelection(communityChats.size() - 1); // Scroll to bottom
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("CommunityChat", "Error fetching username: " + databaseError.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CommunityChat", "Error: " + databaseError.getMessage());
            }
        });
    }



    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            long timestamp = System.currentTimeMillis();

            // First, get the username from the user reference
            userRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    String username = userSnapshot.child("name").getValue(String.class);
                    if (username != null) {
                        // Create the chat item with the username
                        CommunityChatItem chatItem = new CommunityChatItem(currentUserId, username, message, timestamp);

                        // Push the message to Firebase
                        communityChatRef.push().setValue(chatItem).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                messageInput.setText(""); // Clear the input field after sending
                            } else {
                                Log.e("CommunityChat", "Failed to send message");
                            }
                        });
                    } else {
                        Log.e("CommunityChat", "Failed to retrieve username");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("CommunityChat", "Error: " + databaseError.getMessage());
                }
            });
        }
    }
}
