package com.example.fineoutside.activities;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.GroupAdapter;
import com.example.fineoutside.data.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YourGroupsActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("groups");

    private RecyclerView yourGroupsList;

    private Location userLocation;

    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Group> groupList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null && user != null) {
                    Location groupLocation = new Location("Group_location");
                    groupLocation.setLatitude(Double.parseDouble(group.getLatitude()));
                    groupLocation.setLongitude(Double.parseDouble(group.getLongitude()));
                    if (userLocation.distanceTo(groupLocation) <= 1000 && (group.getRegistered_users() != null && group.getRegistered_users().contains(user.getUid()))) {
                        groupList.add(group);
                    }
                }
            }
            yourGroupsList.setAdapter(new GroupAdapter(groupList, groupId -> {
                root.child(groupId).setValue(null);
                Toast.makeText(YourGroupsActivity.this, "Group successfully deleted!", Toast.LENGTH_SHORT).show();
            }));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_groups);
        initAttrs();
        initUI();
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

    private void initAttrs() {
        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    protected void initUI() {
        yourGroupsList = findViewById(R.id.yourGroupsList);
    }
}
