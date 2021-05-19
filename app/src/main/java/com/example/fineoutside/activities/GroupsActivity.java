package com.example.fineoutside.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class GroupsActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("groups");

    private Button create_new_group_button, your_groups_button;
    private SearchView search_groups;
    private RecyclerView groupsList;

    private Location userLocation;

    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Group> groupList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null) {
                    Location groupLocation = new Location("Group_location");
                    groupLocation.setLatitude(Double.parseDouble(group.getLatitude()));
                    groupLocation.setLongitude(Double.parseDouble(group.getLongitude()));
                    if (userLocation.distanceTo(groupLocation) <= 1000) {
                        groupList.add(group);
                    }
                }
            }
            groupsList.setAdapter(new GroupAdapter(groupList, groupId -> {
                root.child(groupId).setValue(null);
                Toast.makeText(GroupsActivity.this, "Group successfully deleted!", Toast.LENGTH_SHORT).show();
            }));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        initAttrs();
        initUI();
        initListeners();

        SharedPreferences userEntrance = getSharedPreferences("Groups", MODE_PRIVATE);
        userEntrance.edit().putInt(user.getUid(), userEntrance.getInt(user.getUid(), 0) + 1).apply();
    }

    @Override
    protected int getFeatureImage() {
        return R.drawable.groups;
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
        super.initUI();
        create_new_group_button = findViewById(R.id.create_new_group_button);
        your_groups_button = findViewById(R.id.your_groups_button);
        search_groups = findViewById(R.id.search_groups);
        groupsList = findViewById(R.id.groupsList);
    }

    private void initListeners() {
        create_new_group_button.setOnClickListener(v -> startActivity(new Intent(GroupsActivity.this, GroupFormActivity.class)));
        your_groups_button.setOnClickListener(v -> startActivity(new Intent(GroupsActivity.this, YourGroupsActivity.class)));
        search_groups.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                GroupAdapter groupAdapter = (GroupAdapter) groupsList.getAdapter();
                if (groupAdapter != null) {
                    groupAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }
}