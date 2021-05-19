package com.example.fineoutside.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.data.WhiteBoardPostChat;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WhiteBoardPostChatAdapter extends RecyclerView.Adapter<WhiteBoardPostChatAdapter.WhiteBoardPostChatViewHolder> {

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final List<WhiteBoardPostChat> whiteBoardPostChatList;

    public WhiteBoardPostChatAdapter(List<WhiteBoardPostChat> whiteBoardPostChatList) {
        this.whiteBoardPostChatList = whiteBoardPostChatList;
    }

    @NonNull
    @Override
    public WhiteBoardPostChatAdapter.WhiteBoardPostChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WhiteBoardPostChatAdapter.WhiteBoardPostChatViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_white_board_post_chat_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WhiteBoardPostChatAdapter.WhiteBoardPostChatViewHolder holder, int position) {
        WhiteBoardPostChat whiteBoardPostChat = whiteBoardPostChatList.get(position);
        holder.user_name.setText(whiteBoardPostChat.getUser_name());
        holder.message.setText(whiteBoardPostChat.getMessage());
        holder.time.setText(whiteBoardPostChat.getTime());

        String pic = whiteBoardPostChat.getPic();

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
    }


    @Override
    public int getItemCount() {
        return whiteBoardPostChatList.size();
    }

    public static class WhiteBoardPostChatViewHolder extends RecyclerView.ViewHolder {

        private final TextView user_name;
        private final TextView message;
        private final TextView time;
        private final ImageView pic;

        public WhiteBoardPostChatViewHolder(@NonNull View itemView) {
            super(itemView);
            user_name = itemView.findViewById(R.id.user_name_text);
            message = itemView.findViewById(R.id.message_text);
            time = itemView.findViewById(R.id.time_text);
            pic = itemView.findViewById(R.id.pic_image);
        }
    }
}