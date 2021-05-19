package com.example.fineoutside.adapters;

import android.content.Intent;
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
import com.example.fineoutside.activities.BasicChatActivity;
import com.example.fineoutside.data.ChildNameAge;
import com.example.fineoutside.data.FamilyProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewPeopleAdapter extends RecyclerView.Adapter<NewPeopleAdapter.NewPeopleViewHolder> implements Filterable {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final List<FamilyProfile> familyProfileList;
    private List<FamilyProfile> familyProfileListFiltered;

    public NewPeopleAdapter(List<FamilyProfile> familyProfileList) {
        this.familyProfileList = familyProfileList;
        this.familyProfileListFiltered = familyProfileList;
    }

    @NonNull
    @Override
    public NewPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewPeopleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_family_profile_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewPeopleViewHolder holder, int position) {
        FamilyProfile familyProfile = familyProfileListFiltered.get(position);
        holder.user_name_text.setText(familyProfile.getUser_name());
        holder.user_age_text.setText(String.valueOf(familyProfile.getAge()));
        if (familyProfile.getChildNameAgeList() == null) {
            holder.num_of_children_text.setVisibility(View.GONE);
            holder.children_ages_text.setVisibility(View.GONE);
        } else {
            holder.num_of_children_text.setVisibility(View.VISIBLE);
            holder.children_ages_text.setVisibility(View.VISIBLE);
            holder.num_of_children_text.setText(new StringBuilder("Num of children: " + familyProfile.getChildNameAgeList().size()));
            StringBuilder ages = new StringBuilder();
            for (ChildNameAge childNameAge : familyProfile.getChildNameAgeList()) {
                ages.append(childNameAge.getChild_age()).append(", ");
            }
            holder.children_ages_text.setText(ages.substring(0, ages.lastIndexOf(", ")));
        }
        String profile_pic = familyProfile.getProfile_pic();
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

        holder.itemView.setOnClickListener(v -> {
            if (user != null) {
                Intent basicChatIntent = new Intent(holder.itemView.getContext(), BasicChatActivity.class);
                basicChatIntent.putExtra("MY_UID", user.getUid());
                basicChatIntent.putExtra("OTHER_UID", familyProfile.getUid());
                basicChatIntent.putExtra("MY_USER_NAME", user.getDisplayName());
                basicChatIntent.putExtra("OTHER_USER_NAME", familyProfile.getUser_name());
                basicChatIntent.putExtra("ROOT", "family profile chat messages");
                basicChatIntent.putExtra("STORAGE", "family_profile_chat_pics");
                holder.itemView.getContext().startActivity(basicChatIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return familyProfileListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    familyProfileListFiltered = familyProfileList;
                } else {
                    List<FamilyProfile> filteredList = new ArrayList<>();
                    for (FamilyProfile familyProfile : familyProfileList) {
                        boolean isAgeInList = false;
                        try {
                            int age = Integer.parseInt(charString);
                            for (ChildNameAge childNameAge : familyProfile.getChildNameAgeList()) {
                                if (childNameAge.getChild_age() == age) {
                                    isAgeInList = true;
                                    break;
                                }
                            }
                        } catch (Exception ignored) {

                        }
                        if (isAgeInList || familyProfile.getUser_name().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(familyProfile);
                        }
                    }
                    familyProfileListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = familyProfileListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                familyProfileListFiltered = (ArrayList<FamilyProfile>) filterResults.values;
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public static class NewPeopleViewHolder extends RecyclerView.ViewHolder {

        private final TextView user_name_text, user_age_text, num_of_children_text, children_ages_text;
        private final ImageView profilePic;

        public NewPeopleViewHolder(@NonNull View itemView) {
            super(itemView);
            user_name_text = itemView.findViewById(R.id.user_name_text);
            user_age_text = itemView.findViewById(R.id.user_age_text);
            num_of_children_text = itemView.findViewById(R.id.num_of_children_text);
            children_ages_text = itemView.findViewById(R.id.children_ages_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image);
        }
    }
}
