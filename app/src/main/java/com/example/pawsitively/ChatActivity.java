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
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private EditText messageInput;
    private ListView messageListView;
    private Button sendButton;

    private MessageAdapter messageAdapter;
    private List<MessageItem> messageList;

    private String currentUserId;
    private String recipientUserId;
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        messageInput = findViewById(R.id.message_input);
        messageListView = findViewById(R.id.message_list);
        sendButton = findViewById(R.id.send_button);

        // Get user IDs
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recipientUserId = getIntent().getStringExtra("recipientUserId");

        if (recipientUserId == null) {
            Log.e("ChatActivity", "Recipient user ID is null. Exiting chat.");
            finish(); // Close the activity if the recipient ID is not found
            return;
        }

        // Set up Firebase reference for messages
        messagesRef = FirebaseDatabase.getInstance().getReference()
                .child("chats").child(currentUserId).child(recipientUserId);

        // Set up message list
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messageListView.setAdapter(messageAdapter);

        // Load messages
        loadMessages();

        // Send message on button click
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MessageItem messageItem = snapshot.getValue(MessageItem.class);
                    if (messageItem != null) {
                        messageList.add(messageItem);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messageListView.setSelection(messageAdapter.getCount() - 1); // Scroll to the last message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ChatActivity", "Error: " + databaseError.getMessage());
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String messageId = messagesRef.push().getKey();
            if (messageId != null) {
                MessageItem messageItem = new MessageItem(currentUserId, messageText, System.currentTimeMillis()); // Include timestamp

                // Save message under the current user's chat reference
                messagesRef.child(messageId).setValue(messageItem)
                        .addOnSuccessListener(aVoid -> {
                            messageInput.setText(""); // Clear input
                        })
                        .addOnFailureListener(e -> Log.e("ChatActivity", "Error: " + e.getMessage()));

                // Save message under the recipient's chat reference as well
                DatabaseReference recipientMessagesRef = FirebaseDatabase.getInstance().getReference()
                        .child("chats").child(recipientUserId).child(currentUserId);
                recipientMessagesRef.child(messageId).setValue(messageItem); // Save to recipient's chat
            }
        }
    }
}
