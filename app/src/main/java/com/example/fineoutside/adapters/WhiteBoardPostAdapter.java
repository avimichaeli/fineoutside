package com.example.fineoutside.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.activities.WhiteBoardActivity;
import com.example.fineoutside.activities.WhiteBoardChatActivity;
import com.example.fineoutside.data.WhiteBoardPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class WhiteBoardPostAdapter extends RecyclerView.Adapter<WhiteBoardPostAdapter.WhiteBoardPostViewHolder> implements Filterable {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();

    private final List<WhiteBoardPost> whiteBoardPostList;
    private final WhiteBoardActivity.OnDeleteWhiteBoardPostListener onDeleteWhiteBoardPostListener;
    private List<WhiteBoardPost> whiteBoardPostListFiltered;

    public WhiteBoardPostAdapter(List<WhiteBoardPost> whiteBoardPostList, WhiteBoardActivity.OnDeleteWhiteBoardPostListener onDeleteWhiteBoardPostListener) {
        this.whiteBoardPostList = whiteBoardPostList;
        this.whiteBoardPostListFiltered = whiteBoardPostList;
        this.onDeleteWhiteBoardPostListener = onDeleteWhiteBoardPostListener;
    }

    @NonNull
    @Override
    public WhiteBoardPostAdapter.WhiteBoardPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WhiteBoardPostAdapter.WhiteBoardPostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_white_board_post_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WhiteBoardPostAdapter.WhiteBoardPostViewHolder holder, int position) {
        WhiteBoardPost whiteBoardPost = whiteBoardPostListFiltered.get(position);
        holder.post_subject_text.setText(whiteBoardPost.getPost_subject());
        holder.post_content_text.setText(whiteBoardPost.getPost_content());
        holder.groups_names_text.setText(whiteBoardPost.getGroups_names().toString());
        holder.delete_post_button.setVisibility(whiteBoardPost.getCreator_id().equalsIgnoreCase(user == null ? "" : user.getUid()) ? View.VISIBLE : View.GONE);
        holder.delete_post_button.setOnClickListener(v -> {
            if (onDeleteWhiteBoardPostListener != null) {
                onDeleteWhiteBoardPostListener.deletePost(whiteBoardPost.getPost_id());
            }
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), WhiteBoardChatActivity.class);
            intent.putExtra("Post_id", whiteBoardPost.getPost_id());
            intent.putExtra("Post_subject", whiteBoardPost.getPost_subject());
            intent.putExtra("Post_content", whiteBoardPost.getPost_content());
            intent.putExtra("Post_groups", whiteBoardPost.getGroups_names().toString());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return whiteBoardPostListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    whiteBoardPostListFiltered = whiteBoardPostList;
                } else {
                    List<WhiteBoardPost> filteredList = new ArrayList<>();
                    for (WhiteBoardPost whiteBoardPost : whiteBoardPostList) {
                        if (whiteBoardPost.getPost_subject().toLowerCase().contains(charString.toLowerCase()) || whiteBoardPost.getPost_content().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(whiteBoardPost);
                        }
                    }
                    whiteBoardPostListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = whiteBoardPostListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                whiteBoardPostListFiltered = (ArrayList<WhiteBoardPost>) filterResults.values;
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public static class WhiteBoardPostViewHolder extends RecyclerView.ViewHolder {

        private final TextView post_subject_text, post_content_text, groups_names_text;
        private final Button delete_post_button;

        public WhiteBoardPostViewHolder(@NonNull View itemView) {
            super(itemView);
            post_subject_text = itemView.findViewById(R.id.post_subject_text);
            post_content_text = itemView.findViewById(R.id.post_content_text);
            groups_names_text = itemView.findViewById(R.id.groups_names_text);
            delete_post_button = itemView.findViewById(R.id.delete_post_button);
        }
    }
}