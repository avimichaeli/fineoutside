package com.example.fineoutside.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.UserAdapter;
import com.example.fineoutside.data.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BabysittingServicesActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");

    private SearchView search_users;
    private RecyclerView users_list;

    private Location userLocation;

    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<UserDetails> userDetailsList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                if (userDetails != null && user != null && userDetails.getUserType().equalsIgnoreCase("Babysitter")) {
                    Location otherUserLocation = new Location("Other_user_location");
                    otherUserLocation.setLatitude(userDetails.getLatitude());
                    otherUserLocation.setLongitude(userDetails.getLongitude());
                    userDetails.setDistance((int) userLocation.distanceTo(otherUserLocation));
                    if (userDetails.getDistance() <= 2000) {
                        userDetailsList.add(userDetails);
                    }
                }
            }
            Collections.sort(userDetailsList);
            users_list.setAdapter(new UserAdapter(userDetailsList, (uid, user_name) -> {
                if (user != null) {
                    root.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            if (userDetails != null) {
                                userDetails.setSelectedBabysitterId(uid);
                                root.child(user.getUid()).setValue(userDetails);
                                Intent basicChatIntent = new Intent(BabysittingServicesActivity.this, BasicChatActivity.class);
                                basicChatIntent.putExtra("MY_UID", user.getUid());
                                basicChatIntent.putExtra("OTHER_UID", uid);
                                basicChatIntent.putExtra("MY_USER_NAME", user.getDisplayName());
                                basicChatIntent.putExtra("OTHER_USER_NAME", user_name);
                                basicChatIntent.putExtra("ROOT", "babysitter chat messages");
                                basicChatIntent.putExtra("STORAGE", "babysitter_chat_pics");
                                startActivity(basicChatIntent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_babysitting_services);
        initUI();
        initAttrs();
        initListeners();

        SharedPreferences userEntrance = getSharedPreferences("BabysittingServices", MODE_PRIVATE);
        userEntrance.edit().putInt(user.getUid(), userEntrance.getInt(user.getUid(), 0) + 1).apply();
    }

    @Override
    protected int getFeatureImage() {
        return R.drawable.babysitter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        root.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        root.removeEventListener(valueEventListener);
        super.onStop();
    }

    protected void initUI() {
        super.initUI();
        search_users = findViewById(R.id.search_users);
        users_list = findViewById(R.id.users_list);
    }

    private void initAttrs() {
        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    private void initListeners() {
        search_users.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                UserAdapter userAdapter = (UserAdapter) users_list.getAdapter();
                if (userAdapter != null) {
                    userAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }
}