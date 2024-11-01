package com.example.pawsitively;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommunityChatAdapter extends ArrayAdapter<CommunityChatItem> {
    private Context context;
    private List<CommunityChatItem> communityChatItems;

    public CommunityChatAdapter(Context context, List<CommunityChatItem> items) {
        super(context, 0, items);
        this.context = context;
        this.communityChatItems = items;

        // Sort items by timestamp on initialization
        Collections.sort(this.communityChatItems, new Comparator<CommunityChatItem>() {
            @Override
            public int compare(CommunityChatItem o1, CommunityChatItem o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });
    }

    static class ViewHolder {
        TextView messageTextView;
        TextView usernameTextView;
        TextView timestampTextView;
        LinearLayout layout; // For controlling layout alignment
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommunityChatItem chatItem = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            // Inflate the layout and initialize the ViewHolder
            convertView = LayoutInflater.from(context).inflate(R.layout.community_chat_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.messageTextView = convertView.findViewById(R.id.message_text);
            viewHolder.usernameTextView = convertView.findViewById(R.id.username_text);
            viewHolder.timestampTextView = convertView.findViewById(R.id.timestamp_text);
            viewHolder.layout = convertView.findViewById(R.id.chat_item_layout);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (chatItem != null) {
            // Set the message, username, and timestamp text
            viewHolder.messageTextView.setText(chatItem.getMessage());
            viewHolder.usernameTextView.setText(chatItem.getUsername());

            // Format the timestamp
            String formattedTime = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date(chatItem.getTimestamp()));
            viewHolder.timestampTextView.setText(formattedTime);

            // Align and set background based on sender
            if (chatItem.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                // Sent messages aligned to the right
                viewHolder.layout.setGravity(Gravity.END);
                viewHolder.usernameTextView.setVisibility(View.GONE);
                viewHolder.messageTextView.setBackgroundResource(R.drawable.message_background_sender); // Light blue for sent messages
            } else {
                // Received messages aligned to the left
                viewHolder.layout.setGravity(Gravity.START);
                viewHolder.usernameTextView.setVisibility(View.VISIBLE);
                viewHolder.messageTextView.setBackgroundResource(R.drawable.message_background_receiver); // Light gray for received messages
            }
        } else {
            Log.e("CommunityChatAdapter", "ChatItem is null for position: " + position);
        }

        return convertView;
    }


    public void refreshData(List<CommunityChatItem> newItems) {
        this.communityChatItems.clear();
        this.communityChatItems.addAll(newItems);

        Collections.sort(this.communityChatItems, new Comparator<CommunityChatItem>() {
            @Override
            public int compare(CommunityChatItem o1, CommunityChatItem o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });
        notifyDataSetChanged();
    }
}
