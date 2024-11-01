package com.example.pawsitively;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;

public class MessageAdapter extends ArrayAdapter<MessageItem> {
    private final List<MessageItem> messages;

    public MessageAdapter(Context context, List<MessageItem> messages) {
        super(context, 0, messages);
        this.messages = messages;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        MessageItem messageItem = messages.get(position);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listItem = inflater.inflate(R.layout.item_message, parent, false);

        TextView messageTextView = listItem.findViewById(R.id.message_text);
        TextView timestampTextView = listItem.findViewById(R.id.timestamp_text); // Add a TextView for the timestamp

        messageTextView.setText(messageItem.getMessage());
        timestampTextView.setText(formatTimestamp(messageItem.getTimestamp())); // Format the timestamp

        if (messageItem.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            messageTextView.setBackgroundResource(R.drawable.message_background_sender);
            // Align to the right
            ((LinearLayout) listItem).setGravity(Gravity.END);
        } else {
            messageTextView.setBackgroundResource(R.drawable.message_background_receiver);
            // Align to the left
            ((LinearLayout) listItem).setGravity(Gravity.START);
        }

        return listItem;
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
