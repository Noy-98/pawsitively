package com.example.pawsitively;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ContactNumberPrivateMessageAdapter extends android.widget.ArrayAdapter<String> {
    private Context context;
    private List<String> contacts;

    public ContactNumberPrivateMessageAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, R.layout.item_private_chat_person_list, objects);
        this.context = context;
        this.contacts = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_private_chat_person_list, parent, false);
        }

        TextView contactNumber = convertView.findViewById(R.id.contactNumber);
        Button viewBttn = convertView.findViewById(R.id.viewBttn);

        String contact = contacts.get(position);
        contactNumber.setText(contact);

        viewBttn.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String currentNumber = snapshot.child("contactNumber").getValue(String.class);

                        if (currentNumber != null) {
                            Intent intent = new Intent(context, PersonalChatActivity.class);
                            intent.putExtra("toContact", contact);
                            intent.putExtra("fromContact", currentNumber);
                            context.startActivity(intent);
                        } else {
                            // Handle the case where the number is not found
                            contactNumber.setText("No number found");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                }
            });
        });

        return convertView;
    }
}
