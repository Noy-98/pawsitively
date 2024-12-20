package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
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

public class PersonalChatActivity extends AppCompatActivity {

    private EditText toEditText, fromEditText, messageInput;
    private Button sendButton;
    private ListView communityChatListView;
    private DatabaseReference privateChatRef;
    private String currentUserId, currentUserContact;
    private PrivateMessageAdapter messageAdapter;
    private ArrayList<CommunityChatItem> chatMessages;

    private FloatingActionButton backButton;

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

        toEditText = findViewById(R.id.to);
        fromEditText = findViewById(R.id.from);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        communityChatListView = findViewById(R.id.communityChatListView);

        backButton = findViewById(R.id.go_back_bttn);

        backButton.setOnClickListener(view -> startActivity(new Intent(PersonalChatActivity.this, PersonalChatListActivity.class)));

        String toContact = getIntent().getStringExtra("toContact");
        String fromContact = getIntent().getStringExtra("fromContact");

        toEditText.setText(toContact);
        fromEditText.setText(fromContact);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        privateChatRef = FirebaseDatabase.getInstance().getReference().child("PrivateChat");

        chatMessages = new ArrayList<>();
        messageAdapter = new PrivateMessageAdapter(this, chatMessages);
        communityChatListView.setAdapter(messageAdapter);

        loadChatMessages(toContact);
        sendButton.setOnClickListener(v -> sendMessage(toContact));
    }

    private void sendMessage(String toContact) {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String conversationKey = generateConversationKey(fromEditText.getText().toString(), toContact);
        long timestamp = System.currentTimeMillis();
        String messageId = privateChatRef.child(conversationKey).push().getKey();

        CommunityChatItem chatItem = new CommunityChatItem(currentUserId, "", toContact, message, timestamp);
        privateChatRef.child(conversationKey).child(messageId).setValue(chatItem).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                messageInput.setText("");
            } else {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChatMessages(String toContact) {
        String conversationKey = generateConversationKey(fromEditText.getText().toString(), toContact);

        privateChatRef.child(conversationKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    CommunityChatItem chatItem = messageSnapshot.getValue(CommunityChatItem.class);
                    if (chatItem != null) {
                        chatMessages.add(chatItem);
                    }
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
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
}
