package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class Message extends AppCompatActivity {
    private EditText searchInput;
    private ListView recentChatsList, searchResultsList;
    private List<ChatItem> recentChats;
    private List<ChatItem> searchResults;
    private DatabaseReference userRef, chatsRef;
    private String currentUserId;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        searchInput = findViewById(R.id.search_input);
        recentChatsList = findViewById(R.id.recent_chats_list);
        searchResultsList = findViewById(R.id.search_results_list);
        TextView communityChatBox = findViewById(R.id.community_chat_box);
        recentChats = new ArrayList<>();
        searchResults = new ArrayList<>();

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        chatsRef = FirebaseDatabase.getInstance().getReference().child("chats").child(currentUserId);

        chatAdapter = new ChatAdapter(this, recentChats);
        recentChatsList.setAdapter(chatAdapter);

        loadRecentChats();

        recentChatsList.setOnItemClickListener((parent, view, position, id) -> {
            ChatItem chatItem = recentChats.get(position);
            openChat(chatItem.getUserId());
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        communityChatBox.setOnClickListener(v -> openCommunityChat());
    }

    private void loadRecentChats() {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recentChats.clear();
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    String recipientId = chatSnapshot.getKey();
                    long lastMessageTimestamp = 0;
                    for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                        Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);
                        if (timestamp != null) {
                            lastMessageTimestamp = timestamp;
                        }
                    }
                    loadUserProfile(recipientId, lastMessageTimestamp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Message", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void loadUserProfile(String userId, long lastMessageTimestamp) {
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue(String.class);
                String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

                if (username != null && profileImageUrl != null) {
                    ChatItem chatItem = new ChatItem(userId, username, profileImageUrl, lastMessageTimestamp);
                    recentChats.add(chatItem);
                    chatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Message", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void searchUsers(String query) {
        if (query.isEmpty()) {
            searchResults.clear();
            searchResultsList.setVisibility(View.GONE);
            recentChatsList.setVisibility(View.VISIBLE); // Show recent chats when the search is empty

            // Show the community chat box when the search is empty
            findViewById(R.id.community_chat_box).setVisibility(View.VISIBLE);
            return;
        }

        // Hide recent chats and community chat box while searching
        recentChatsList.setVisibility(View.GONE);
        findViewById(R.id.community_chat_box).setVisibility(View.GONE);
 
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchResults.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    String userId = snapshot.getKey();

                    if (name != null && name.toLowerCase().contains(query.toLowerCase())) {
                        ChatItem user = new ChatItem(userId, name, profileImageUrl, 0);
                        searchResults.add(user);
                    }
                }

                ChatAdapter searchAdapter = new ChatAdapter(Message.this, searchResults);
                searchResultsList.setAdapter(searchAdapter);
                searchResultsList.setVisibility(View.VISIBLE);

                searchResultsList.setOnItemClickListener((parent, view, position, id) -> {
                    ChatItem user = searchResults.get(position);
                    openChat(user.getUserId());
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Message", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void openChat(String recipientUserId) {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("recipientUserId", recipientUserId);
        startActivity(chatIntent);
    }

    private void openCommunityChat() {
        Intent chatIntent = new Intent(this, CommunityChatActivity.class);
        startActivity(chatIntent);
    }
}
