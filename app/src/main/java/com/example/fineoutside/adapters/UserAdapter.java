package com.example.fineoutside.adapters;

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

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.data.UserDetails;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements Filterable {

    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private final List<UserDetails> userDetailsList;
    private final ConnectListener connectListener;
    private List<UserDetails> userDetailsListFiltered;

    public UserAdapter(List<UserDetails> userDetailsList, ConnectListener connectListener) {
        this.userDetailsList = userDetailsList;
        this.userDetailsListFiltered = userDetailsList;
        this.connectListener = connectListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_user_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        UserDetails userDetails = userDetailsListFiltered.get(position);
        String pic = userDetails.getPic();
        if (pic != null && !pic.isEmpty()) {
            holder.pic_image.setVisibility(View.VISIBLE);
            if (pic.startsWith("http")) {
                Glide.with(holder.itemView.getContext()).load(pic).circleCrop().into(holder.pic_image);
            } else {
                try {
                    File file = File.createTempFile("images", "jpg");
                    storage.getReference(pic).getFile(file).addOnCompleteListener(task ->
                        Glide.with(holder.itemView.getContext()).load(file).circleCrop().into(holder.pic_image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            holder.pic_image.setVisibility(View.GONE);
        }
        String name = userDetails.getName();
        if (name != null && !name.isEmpty()) {
            holder.name_text.setText(name);
            holder.name_text.setVisibility(View.VISIBLE);
        } else {
            holder.name_text.setVisibility(View.GONE);
        }
        String age = userDetails.getAge();
        if (age != null && !age.isEmpty()) {
            holder.age_text.setText(age);
            holder.age_text.setVisibility(View.VISIBLE);
        } else {
            holder.age_text.setVisibility(View.GONE);
        }
        String aboutMe = userDetails.getAbout_me();
        if (aboutMe != null && !aboutMe.isEmpty()) {
            holder.about_me_text.setText(aboutMe);
            holder.about_me_text.setVisibility(View.VISIBLE);
        } else {
            holder.about_me_text.setVisibility(View.GONE);
        }
        holder.distance_text.setText(new StringBuilder(userDetails.getDistance() + "\nmeters"));
        holder.connect_button.setOnClickListener(v -> {
            if (connectListener != null) {
                connectListener.onConnect(userDetails.getUid(), userDetails.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userDetailsListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    userDetailsListFiltered = userDetailsList;
                } else {
                    List<UserDetails> filteredList = new ArrayList<>();
                    for (UserDetails userDetails : userDetailsList) {
                        if (userDetails.getName().toLowerCase().contains(charString.toLowerCase()) || (userDetails.getAge() != null && userDetails.getAge().toLowerCase().contains(charString.toLowerCase()))) {
                            filteredList.add(userDetails);
                        }
                    }
                    userDetailsListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = userDetailsListFiltered;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                userDetailsListFiltered = (ArrayList<UserDetails>) filterResults.values;
                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    public interface ConnectListener {
        void onConnect(String uid, String user_name);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        private final ImageView pic_image;
        private final TextView name_text, age_text, about_me_text, distance_text;
        private final Button connect_button;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            pic_image = itemView.findViewById(R.id.pic_image);
            name_text = itemView.findViewById(R.id.name_text);
            age_text = itemView.findViewById(R.id.age_text);
            about_me_text = itemView.findViewById(R.id.about_me_text);
            distance_text = itemView.findViewById(R.id.distance_text);
            connect_button = itemView.findViewById(R.id.connect_button);
        }
    }
}
