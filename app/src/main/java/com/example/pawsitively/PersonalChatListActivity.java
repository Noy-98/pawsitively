package com.example.pawsitively;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class PersonalChatListActivity extends AppCompatActivity {

    private EditText toEditText, fromEditText;
    private Button textBttn;
    //private ListView personChatList;
    private DatabaseReference usersRef;
    private String currentUserId;

    private FloatingActionButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_chat_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toEditText = findViewById(R.id.to);
        fromEditText = findViewById(R.id.from);
        textBttn = findViewById(R.id.textBttn);
       // personChatList = findViewById(R.id.personChatList);

        backButton = findViewById(R.id.go_back_bttn);

        backButton.setOnClickListener(view -> startActivity(new Intent(PersonalChatListActivity.this, CommunityChatActivity.class)));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Fetch and display the current user's contact number in the "from" EditText
        usersRef.child(currentUserId).child("contactNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String contactNumber = snapshot.getValue(String.class);
                    if (contactNumber != null) {
                        fromEditText.setText(contactNumber);
                    }
                } else {
                    Toast.makeText(PersonalChatListActivity.this, "Failed to fetch contact number.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalChatListActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        textBttn.setOnClickListener(v -> {
            String to = toEditText.getText().toString().trim();
            String from = fromEditText.getText().toString().trim();

            if (to.isEmpty() || from.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(PersonalChatListActivity.this, PersonalChatActivity.class);
                intent.putExtra("toContact", to);
                intent.putExtra("fromContact", from);
                startActivity(intent);
            }
        });

       // loadChatContacts();
    }

   /* private void loadChatContacts() {
        DatabaseReference privateChatRef = FirebaseDatabase.getInstance().getReference().child("PrivateChat");

        privateChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> contacts = new ArrayList<>();
                String currentUserContact = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

                for (DataSnapshot conversationSnapshot : snapshot.getChildren()) {
                    String conversationKey = conversationSnapshot.getKey();

                    if (conversationKey != null && currentUserContact != null && conversationKey.contains(currentUserContact)) {
                        // Extract the other participant's contact
                        String[] participants = conversationKey.split("_");
                        String otherContact = participants[0].equals(currentUserContact) ? participants[1] : participants[0];

                        contacts.add(otherContact);
                    }
                }

                if (contacts.isEmpty()) {
                    Toast.makeText(PersonalChatListActivity.this, "No conversations found.", Toast.LENGTH_SHORT).show();
                } else {
                    ContactNumberPrivateMessageAdapter adapter = new ContactNumberPrivateMessageAdapter(PersonalChatListActivity.this, contacts);
                    personChatList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalChatListActivity.this, "Failed to load conversations: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    } */

}
