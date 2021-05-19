package com.example.fineoutside.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.NewPeopleAdapter;
import com.example.fineoutside.data.FamilyProfile;
import com.example.fineoutside.data.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MeetingNewPeopleActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");

    private Button familyProfileButton;
    private TextView no_family_profile_message_text;
    private final ValueEventListener messageValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            UserDetails userDetails = snapshot.getValue(UserDetails.class);
            no_family_profile_message_text.setVisibility((userDetails == null || userDetails.getFamilyProfile() == null) ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private SearchView search_new_people;
    private RecyclerView newPeopleList;
    private Location userLocation;
    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<FamilyProfile> familyProfileList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                if (userDetails != null) {
                    FamilyProfile familyProfile = userDetails.getFamilyProfile();
                    if (familyProfile != null && !userDetails.getUid().equalsIgnoreCase(user == null ? "" : user.getUid())) {
                        Location location = new Location("Location");
                        location.setLatitude(familyProfile.getLatitude());
                        location.setLongitude(familyProfile.getLongitude());
                        if (userLocation.distanceTo(location) <= 2000) {
                            familyProfileList.add(familyProfile);
                        }
                    }
                }
            }
            newPeopleList.setAdapter(new NewPeopleAdapter(familyProfileList));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_new_people);
        initAttrs();
        initUI();
        initListeners();

        SharedPreferences userEntrance = getSharedPreferences("MeetingNewPeople", MODE_PRIVATE);
        userEntrance.edit().putInt(user.getUid(), userEntrance.getInt(user.getUid(), 0) + 1).apply();
    }

    @Override
    protected int getFeatureImage() {
        return R.drawable.meeting_new_people;
    }

    @Override
    protected void onStart() {
        super.onStart();
        root.addValueEventListener(valueEventListener);
        root.child(user.getUid()).addValueEventListener(messageValueEventListener);
    }

    @Override
    protected void onStop() {
        root.removeEventListener(valueEventListener);
        root.child(user.getUid()).removeEventListener(messageValueEventListener);
        super.onStop();
    }

    private void initAttrs() {
        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    protected void initUI() {
        super.initUI();
        familyProfileButton = findViewById(R.id.family_profile_button);
        no_family_profile_message_text = findViewById(R.id.no_family_profile_message_text);
        search_new_people = findViewById(R.id.search_new_people);
        newPeopleList = findViewById(R.id.new_people_list);
    }

    private void initListeners() {
        familyProfileButton.setOnClickListener(v -> root.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDetails userDetails = snapshot.getValue(UserDetails.class);
                if (userDetails != null) {
                    Intent familyProfileIntent = new Intent(MeetingNewPeopleActivity.this, FamilyProfileActivity.class);
                    if (userDetails.getFamilyProfile() != null) {
                        familyProfileIntent.putExtra("FamilyProfile", userDetails.getFamilyProfile());
                    }
                    startActivity(familyProfileIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
        search_new_people.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                NewPeopleAdapter newPeopleAdapter = (NewPeopleAdapter) newPeopleList.getAdapter();
                if (newPeopleAdapter != null) {
                    newPeopleAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }
}