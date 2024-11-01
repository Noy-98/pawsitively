package com.example.pawsitively;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
public class ChatAdapter extends BaseAdapter {
    private Context context;
    private List<ChatItem> chatList;

    public ChatAdapter(Context context, List<ChatItem> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public ChatItem getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_item_layout, parent, false);
        }

        ImageView profileImageView = convertView.findViewById(R.id.profile);
        TextView usernameView = convertView.findViewById(R.id.username);
        TextView lastMessageTimeView = convertView.findViewById(R.id.last_message_time);

        ChatItem chatItem = getItem(position);

        usernameView.setText(chatItem.getUsername());
        Glide.with(context).load(chatItem.getProfileImageUrl()).into(profileImageView);

        long timestamp = chatItem.getLastMessageTimestamp();
        if (timestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            lastMessageTimeView.setText(sdf.format(timestamp));
        } else {
            lastMessageTimeView.setText(""); // Empty if no message yet
        }

        return convertView;
    }
}
