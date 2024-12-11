package com.example.pawsitively;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class PrivateMessageAdapter extends android.widget.ArrayAdapter<CommunityChatItem> {

    private Context context;
    private List<CommunityChatItem> privateMessages;

    public PrivateMessageAdapter(@NonNull Context context, @NonNull List<CommunityChatItem> objects) {
        super(context, 0, objects);
        this.context = context;
        this.privateMessages = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CommunityChatItem chatItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_private_message_item, parent, false);
        }

        // Initialize UI components
        TextView messageText = convertView.findViewById(R.id.message_text);
        TextView timestampText = convertView.findViewById(R.id.timestamp_text);
        LinearLayout chatItemLayout = convertView.findViewById(R.id.chat_item_layout);

        // Set message and timestamp
        if (chatItem != null) {
            messageText.setText(chatItem.getMessage());
            String formattedTime = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date(chatItem.getTimestamp()));
            timestampText.setText(formattedTime);

            // Align messages
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (chatItem.getSenderId().equals(currentUserId)) {
                chatItemLayout.setGravity(View.TEXT_ALIGNMENT_VIEW_END); // Right-align for sent messages
                messageText.setBackgroundResource(R.drawable.message_background_sender); // Customize sender background
            } else {
                chatItemLayout.setGravity(View.TEXT_ALIGNMENT_VIEW_START); // Left-align for received messages
                messageText.setBackgroundResource(R.drawable.message_background_receiver); // Customize receiver background
            }
        }

        return convertView;
    }
}
