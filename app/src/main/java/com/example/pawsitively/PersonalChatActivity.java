package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PersonalChatActivity extends AppCompatActivity {

    private FloatingActionButton go_back_bttn;
    private EditText toEditText, fromEditText, messageInput;
    private Button sendButton;
    private ListView communityChatListView;
    private String currentUserId;
    private String currentUserContact;
    private DatabaseReference usersRef, privateChatRef;
    private PrivateMessageAdapter privateMessageAdapter;
    private List<CommunityChatItem> privateChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_chat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        toEditText = findViewById(R.id.to);
        fromEditText = findViewById(R.id.from);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        communityChatListView = findViewById(R.id.communityChatListView);
        go_back_bttn = findViewById(R.id.go_back_bttn);

        privateChats = new ArrayList<>();
        privateMessageAdapter = new PrivateMessageAdapter(this, privateChats);
        communityChatListView.setAdapter(privateMessageAdapter);

        // Firebase references
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        privateChatRef = FirebaseDatabase.getInstance().getReference().child("PrivateChat");

        // Load current user's contact number
        loadCurrentUserContact();

        sendButton.setOnClickListener(v -> sendPrivateMessage());

        go_back_bttn.setOnClickListener(view -> startActivity(new Intent(PersonalChatActivity.this, CommunityChatActivity.class)));
    }

    private void sendPrivateMessage() {
        String toContact = toEditText.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        if (toContact.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String conversationKey = generateConversationKey(currentUserContact, toContact);

        usersRef.orderByChild("contactNumber").equalTo(toContact).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long timestamp = System.currentTimeMillis();
                    String messageId = privateChatRef.child(conversationKey).push().getKey();

                    CommunityChatItem chatItem = new CommunityChatItem(currentUserId, "", toContact, message, timestamp);

                    privateChatRef.child(conversationKey).child(messageId).setValue(chatItem).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                        } else {
                            Toast.makeText(PersonalChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(PersonalChatActivity.this, "No user found with this contact number!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PersonalChat", "Error verifying contact: " + error.getMessage());
            }
        });
    }

    private String generateConversationKey(String contact1, String contact2) {
        if (contact1.compareTo(contact2) < 0) {
            return contact1 + "_" + contact2;
        } else {
            return contact2 + "_" + contact1;
        }
    }

    private void loadPrivateMessages() {
        String toContact = toEditText.getText().toString().trim();

        if (toContact.isEmpty()) {
            Toast.makeText(PersonalChatActivity.this, "Please enter a recipient contact", Toast.LENGTH_SHORT).show();
            Log.e("PersonalChat", "Recipient contact is empty");
            return;
        }

        if (currentUserContact == null) {
            Toast.makeText(PersonalChatActivity.this, "Current user contact is not loaded", Toast.LENGTH_SHORT).show();
            Log.e("PersonalChat", "Current user contact is null");
            return;
        }

        String conversationKey = generateConversationKey(currentUserContact, toContact);

        privateChatRef.child(conversationKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                privateChats.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    CommunityChatItem chatItem = messageSnapshot.getValue(CommunityChatItem.class);
                    if (chatItem != null) {
                        privateChats.add(chatItem);
                    }
                }
                privateMessageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PersonalChat", "Error loading private messages: " + error.getMessage());
            }
        });
    }


    private void loadCurrentUserContact() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("contactNumber")) {
                    currentUserContact = snapshot.child("contactNumber").getValue(String.class);
                    if (currentUserContact != null) {
                        fromEditText.setText(currentUserContact);
                        loadPrivateMessages(); // Load messages only after contact is set
                    } else {
                        Log.e("PersonalChat", "Contact number is null for current user");
                    }
                } else {
                    Log.e("PersonalChat", "No contactNumber found for current user in database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PersonalChat", "Error loading current user contact: " + error.getMessage());
            }
        });
    }

}
