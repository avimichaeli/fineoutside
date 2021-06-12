package com.example.fineoutside.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.activities.MapsActivity;
import com.example.fineoutside.activities.ParentCommunityOrEmergencyCenterChatActivity;
import com.example.fineoutside.activities.UserMessagesActivity;
import com.example.fineoutside.data.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> implements Filterable {

    private final List<Message> messages;
    private final boolean enableOpenChat, enableOpenUserMessages, isEmergency;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private List<Message> messageListFiltered;

    public MessageAdapter(List<Message> messages, boolean enableOpenChat, boolean enableOpenUserMessages, boolean isEmergency) {
        this.messages = messages;
        this.messageListFiltered = messages;
        this.enableOpenChat = enableOpenChat;
        this.enableOpenUserMessages = enableOpenUserMessages;
        this.isEmergency = isEmergency;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Message message = messageListFiltered.get(position);
        String id = message.getId();
        String user_name = message.getUser_name();
        String message_content = message.getMessage_content();
        String profile_pic = message.getProfile_pic();
        String time = message.getTime();
        String latitude = message.getLatitude();
        String longitude = message.getLongitude();
        String post_latitude = message.getPost_latitude();
        String post_longitude = message.getPost_longitude();
        List<String> watchesList = message.getWatches();
        String watches_counter = String.valueOf(watchesList.size());
        String pic = message.getPic();
        Boolean status = message.getStatus();

        holder.userName.setText(user_name);
        holder.message.setText(message_content);
        if (profile_pic != null && !profile_pic.isEmpty()) {
            if (profile_pic.startsWith("http")) {
                Glide.with(holder.itemView.getContext()).load(profile_pic).circleCrop().into(holder.profilePic);
            } else {
                try {
                    File file = File.createTempFile("images", "jpg");
                    storage.getReference(profile_pic).getFile(file).addOnCompleteListener(task ->
                        Glide.with(holder.itemView.getContext()).load(file).circleCrop().into(holder.profilePic));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        holder.time.setText(time);
        holder.watches.setText(watches_counter);
        holder.watches.setVisibility(enableOpenChat ? View.VISIBLE : View.GONE);
        holder.itemView.setBackgroundColor(isEmergency ? Color.RED : Color.parseColor("#dbbe84"));
        // Verify it's ok to open the chat screen
        if (enableOpenChat) {
            holder.itemView.setOnClickListener(v -> {
                Intent chatIntent = new Intent(holder.itemView.getContext(), ParentCommunityOrEmergencyCenterChatActivity.class);
                // Passing the message id to chat screen
                chatIntent.putExtra("Id", id);
                // Passing the user name to chat screen
                chatIntent.putExtra("User_name", user_name);
                // Passing the first message to chat screen
                chatIntent.putExtra("First_message", message_content);
                // Passing the message time to chat screen
                chatIntent.putExtra("Time", time);
                // Passing the profile pic to chat screen
                chatIntent.putExtra("Profile_pic", profile_pic);
                // Passing the watches counter to chat screen
                chatIntent.putExtra("Watches_counter", watches_counter);
                // Passing the chat status to chat screen
                chatIntent.putExtra("Status", status == null);
                if (post_latitude != null && post_longitude != null) {
                    // Passing the post latitude to chat screen
                    chatIntent.putExtra("Post_latitude", post_latitude);
                    // Passing the post longitude to chat screen
                    chatIntent.putExtra("Post_longitude", post_longitude);
                }
                // Opening new chat screen
                holder.itemView.getContext().startActivity(chatIntent);
            });

            int unseenCount = message.getUnseen_count();

            if (message.getUser_name().equalsIgnoreCase(user == null ? "" : user.getDisplayName()) && unseenCount > 0) {
                holder.unseenCount.setText(String.valueOf(unseenCount));
                holder.unseenCount.setVisibility(View.VISIBLE);
            } else {
                holder.unseenCount.setVisibility(View.GONE);
            }
        } else {
            holder.unseenCount.setVisibility(View.GONE);
            holder.itemView.setOnLongClickListener(v -> {
                if (enableOpenUserMessages && !message.getUser_name().equalsIgnoreCase(user == null ? "" : user.getDisplayName())) {
                    Intent userMessagesIntent = new Intent(holder.itemView.getContext(), UserMessagesActivity.class);
                    // Passing the message id to user messages screen
                    userMessagesIntent.putExtra("Post_id", message.getPost_id());
                    // Passing the user name to user messages screen
                    userMessagesIntent.putExtra("User_name", user_name);
                    // Opening user messages screen
                    holder.itemView.getContext().startActivity(userMessagesIntent);
                }
                return false;
            });
        }

        if (latitude != null && longitude != null && !latitude.isEmpty() && !longitude.isEmpty() && message_content.isEmpty()) {
            holder.message.setText(R.string.shared_location);
            holder.message.setOnClickListener(v -> {
                Intent mapsIntent = new Intent(v.getContext(), MapsActivity.class);
                mapsIntent.putExtra("latitude", latitude);
                mapsIntent.putExtra("longitude", longitude);
                v.getContext().startActivity(mapsIntent);
            });
        }

        if (pic != null && !pic.isEmpty()) {
            try {
                File file = File.createTempFile("images", "jpg");
                storage.getReference(pic).getFile(file).addOnCompleteListener(task ->
                    Glide.with(holder.itemView.getContext()).load(file).into(holder.pic));
                holder.pic.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            holder.pic.setVisibility(View.GONE);
        }

        if (!user_name.equalsIgnoreCase(user == null ? "" : user.getDisplayName()) && latitude != null && longitude != null && post_latitude != null && post_longitude != null &&
            !latitude.isEmpty() && !longitude.isEmpty() && !post_latitude.isEmpty() && !post_longitude.isEmpty()) {
            holder.distance.setVisibility(View.VISIBLE);
            Location location1 = new Location("location1");
            location1.setLatitude(Double.parseDouble(latitude));
            location1.setLongitude(Double.parseDouble(longitude));
            Location location2 = new Location("location2");
            location2.setLatitude(Double.parseDouble(post_latitude));
            location2.setLongitude(Double.parseDouble(post_longitude));
            int distance = (int) location1.distanceTo(location2);
            if (distance > 0) {
                holder.distance.setText(new StringBuilder(distance + " meters"));
            } else {
                holder.distance.setVisibility(View.GONE);
            }
        } else {
            holder.distance.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    messageListFiltered = messages;
                } else {
                    List<Message> filteredList = new ArrayList<>();
                    for (Message message : messages) {
                        if (message.getMessage_content().toLowerCase().contains(charString.toLowerCase()) || message.getUser_name().toLowerCase().contains(charString.toLowerCase()) || message.getTime().contains(charString)) {
                            filteredList.add(message);
                        }
                    }
                    messageListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = messageListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                messageListFiltered = (ArrayList<Message>) filterResults.values;
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        final ImageView profilePic, pic;
        final TextView userName;
        final TextView message;
        final TextView time;
        final TextView watches;
        final TextView distance;
        final TextView unseenCount;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.profile_pic_image);
            userName = itemView.findViewById(R.id.user_name_text);
            message = itemView.findViewById(R.id.message_text);
            time = itemView.findViewById(R.id.time_text);
            watches = itemView.findViewById(R.id.watches_counter_text);
            pic = itemView.findViewById(R.id.pic_image);
            distance = itemView.findViewById(R.id.distance_text);
            unseenCount = itemView.findViewById(R.id.unseen_count_text);
        }
    }
}
