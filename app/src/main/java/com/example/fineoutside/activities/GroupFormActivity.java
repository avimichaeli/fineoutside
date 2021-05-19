package com.example.fineoutside.activities;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fineoutside.R;
import com.example.fineoutside.data.Group;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupFormActivity extends LocationActivity implements OnMapReadyCallback {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("groups");

    private EditText group_name_edit, group_description_edit;
    private Button finish_button, join_or_leave_group_button;

    private Location userLocation;
    private LatLng chosenLocation;

    private String group_name, group_description;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initAttrs();
        initUI();
        fillDetails();
        initListeners();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        chosenLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(chosenLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chosenLocation, 17));

        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            chosenLocation = latLng;
            googleMap.addMarker(new MarkerOptions().position(chosenLocation));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chosenLocation, 17));
        });
    }

    private void initAttrs() {
        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        group_name = getIntent().getStringExtra("Group_name");
        group_description = getIntent().getStringExtra("Group_description");
        if (group_name != null && group_description != null) {
            double group_latitude = Double.parseDouble(getIntent().getStringExtra("Group_latitude"));
            double group_longitude = Double.parseDouble(getIntent().getStringExtra("Group_longitude"));
            userLocation.setLatitude(group_latitude);
            userLocation.setLongitude(group_longitude);
        }
    }

    protected void initUI() {
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        mapFragment.getMapAsync(this);

        group_name_edit = findViewById(R.id.group_name_edit);
        group_description_edit = findViewById(R.id.group_description_edit);
        finish_button = findViewById(R.id.finish_button);
        join_or_leave_group_button = findViewById(R.id.join_or_leave_group_button);
    }

    private void fillDetails() {
        if (group_name != null && group_description != null) {
            group_name_edit.setText(group_name);
            group_name_edit.setFocusable(false);
            group_name_edit.setClickable(false);

            group_description_edit.setText(group_name);
            group_description_edit.setFocusable(false);
            group_description_edit.setClickable(false);

            finish_button.setVisibility(View.GONE);
            join_or_leave_group_button.setVisibility(View.VISIBLE);
        }
    }

    private void initListeners() {
        finish_button.setOnClickListener(v -> {
            String groupName = group_name_edit.getText().toString();
            if (groupName.trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid group name", Toast.LENGTH_SHORT).show();
                return;
            }
            String groupDescription = group_description_edit.getText().toString();
            if (groupDescription.trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid group description", Toast.LENGTH_SHORT).show();
                return;
            }
            List<String> registered_users = new ArrayList<>();
            registered_users.add(user.getUid());
            DatabaseReference ref = root.push();
            ref.setValue(new Group(ref.getKey(), groupName, groupDescription, chosenLocation.latitude + "", chosenLocation.longitude + "", user.getUid(), registered_users));
            Toast.makeText(GroupFormActivity.this, "The group has successfully created", Toast.LENGTH_SHORT).show();
            finish();
        });
        join_or_leave_group_button.setOnClickListener(v -> root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Group group = dataSnapshot.getValue(Group.class);
                    if (group != null && group.getName().equalsIgnoreCase(group_name) && group.getDescription().equalsIgnoreCase(group_description)) {
                        List<String> registered_users = group.getRegistered_users();
                        boolean hasJoined;
                        if (registered_users == null) {
                            registered_users = new ArrayList<>();
                        }
                        if (registered_users.contains(user.getUid())) {
                            registered_users.remove(user.getUid());
                            hasJoined = false;
                        } else {
                            registered_users.add(user.getUid());
                            hasJoined = true;
                        }
                        group.setRegistered_users(registered_users.isEmpty() ? null : registered_users);
                        root.child(dataSnapshot.getKey() == null ? "" : dataSnapshot.getKey()).setValue(group);
                        Toast.makeText(GroupFormActivity.this, "You " + (hasJoined ? "joined to" : "left") + " this group", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));
    }
}
