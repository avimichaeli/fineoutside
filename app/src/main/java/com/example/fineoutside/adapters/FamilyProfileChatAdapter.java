package com.example.fineoutside.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.activities.MapsActivity;
import com.example.fineoutside.data.BasicChat;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FamilyProfileChatAdapter extends RecyclerView.Adapter<FamilyProfileChatAdapter.FamilyProfileChatViewHolder> {

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final List<BasicChat> basicChats;

    public FamilyProfileChatAdapter(List<BasicChat> basicChats) {
        this.basicChats = basicChats;
    }

    @NonNull
    @Override
    public FamilyProfileChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FamilyProfileChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_family_profile_chat_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyProfileChatViewHolder holder, int position) {
        BasicChat basicChat = basicChats.get(position);

        holder.userName.setText(basicChat.getMy_user_name());
        holder.message.setText(basicChat.getMessage());
        holder.time.setText(basicChat.getTime());

        String pic = basicChat.getPic();

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

        String latitude = basicChat.getLatitude();
        String longitude = basicChat.getLongitude();

        if (latitude != null && longitude != null && !latitude.isEmpty() && !longitude.isEmpty() && basicChat.getMessage().isEmpty()) {
            holder.message.setText(R.string.shared_location);
            holder.message.setOnClickListener(v -> {
                Intent mapsIntent = new Intent(v.getContext(), MapsActivity.class);
                mapsIntent.putExtra("latitude", latitude);
                mapsIntent.putExtra("longitude", longitude);
                v.getContext().startActivity(mapsIntent);
            });
        }

        String profile_pic = basicChat.getProfile_pic();

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
    }

    @Override
    public int getItemCount() {
        return basicChats.size();
    }

    public static class FamilyProfileChatViewHolder extends RecyclerView.ViewHolder {

        private final TextView userName;
        private final TextView message;
        private final TextView time;
        private final ImageView pic;
        private final ImageView profilePic;

        public FamilyProfileChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name_text);
            message = itemView.findViewById(R.id.message_text);
            time = itemView.findViewById(R.id.time_text);
            pic = itemView.findViewById(R.id.pic_image);
            profilePic = itemView.findViewById(R.id.profile_pic_image);
        }
    }
}
