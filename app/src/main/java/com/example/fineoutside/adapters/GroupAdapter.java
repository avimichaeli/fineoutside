package com.example.fineoutside.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.activities.GroupFormActivity;
import com.example.fineoutside.data.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> implements Filterable {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();

    private final List<Group> groupList;
    private List<Group> groupListFiltered;
    private final DeleteGroupListener deleteGroupListener;

    public GroupAdapter(List<Group> groupList, DeleteGroupListener deleteGroupListener) {
        this.groupList = groupList;
        this.groupListFiltered = groupList;
        this.deleteGroupListener = deleteGroupListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupListFiltered.get(position);
        holder.group_name_text.setText(group.getName());
        holder.group_description_text.setText(group.getDescription());
        holder.my_group_image.setVisibility(user != null && group.getRegistered_users() != null && group.getRegistered_users().contains(user.getUid()) ? View.VISIBLE : View.INVISIBLE);
        holder.delete_group_button.setVisibility(group.getCreator_id().equalsIgnoreCase(user == null ? "" : user.getUid()) ? View.VISIBLE : View.INVISIBLE);
        holder.delete_group_button.setOnClickListener(v -> {
            if (deleteGroupListener != null) {
                deleteGroupListener.onDeleteGroup(group.getId());
            }
        });
        holder.itemView.setOnClickListener(v -> {
            Intent groupIntent = new Intent(holder.itemView.getContext(), GroupFormActivity.class);
            groupIntent.putExtra("Group_name", group.getName());
            groupIntent.putExtra("Group_description", group.getDescription());
            groupIntent.putExtra("Group_latitude", group.getLatitude());
            groupIntent.putExtra("Group_longitude", group.getLongitude());
            holder.itemView.getContext().startActivity(groupIntent);
        });
    }

    @Override
    public int getItemCount() {
        return groupListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    groupListFiltered = groupList;
                } else {
                    List<Group> filteredList = new ArrayList<>();
                    for (Group group : groupList) {
                        if (group.getName().toLowerCase().contains(charString.toLowerCase()) || group.getDescription().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(group);
                        }
                    }
                    groupListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = groupListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                groupListFiltered = (ArrayList<Group>) filterResults.values;
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        private final TextView group_name_text, group_description_text;
        private final ImageView my_group_image;
        private final Button delete_group_button;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            group_name_text = itemView.findViewById(R.id.group_name_text);
            group_description_text = itemView.findViewById(R.id.group_description_text);
            my_group_image = itemView.findViewById(R.id.my_group_image);
            delete_group_button = itemView.findViewById(R.id.delete_group_button);
        }
    }

    public interface DeleteGroupListener {
        void onDeleteGroup(String groupId);
    }
}
