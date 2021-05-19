package com.example.fineoutside.activities;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fineoutside.R;
import com.example.fineoutside.data.Order;
import com.example.fineoutside.data.UserDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.List;

public class ChooseBusinessOwnerActivity extends LocationActivity implements OnMapReadyCallback {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("food orders");

    private List<UserDetails> userDetailsList;
    private Order order;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_business_owner);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        Location userLocation = new Location("User_location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        userDetailsList = getIntent().getParcelableArrayListExtra("UserDetailsList");

        for (UserDetails userDetails : userDetailsList) {
            Location businessOwnerLocation = new Location("Business_owner_location");
            businessOwnerLocation.setLatitude(userDetails.getLatitude());
            businessOwnerLocation.setLongitude(userDetails.getLongitude());
            userDetails.setDistance((int) businessOwnerLocation.distanceTo(userLocation));
        }

        Collections.sort(userDetailsList);

        order = getIntent().getParcelableExtra("Order");

        userDetailsList.get(0).setSelected(true);
        order.setTookOrderUserId(userDetailsList.get(0).getUid());

        ListView userDetailsListView = findViewById(R.id.userDetailsList);

        userDetailsListView.setAdapter(new ArrayAdapter<UserDetails>(ChooseBusinessOwnerActivity.this, android.R.layout.simple_list_item_1, userDetailsList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                UserDetails userDetails = userDetailsList.get(position);
                ((TextView) view).setText(new StringBuilder(userDetails.getName() + ", " + userDetails.getBusinessName()));
                ((TextView) view).setTypeface(Typeface.DEFAULT, userDetails.isSelected() ? Typeface.BOLD : Typeface.NORMAL);
                view.setOnClickListener(v -> {
                    for (UserDetails userDetails1 : userDetailsList) {
                        userDetails1.setSelected(false);
                    }
                    userDetails.setSelected(true);
                    order.setTookOrderUserId(userDetails.getUid());
                    LatLng chosenLocation = new LatLng(userDetails.getLatitude(), userDetails.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(chosenLocation));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chosenLocation, 17));
                    notifyDataSetChanged();
                });
                return view;
            }
        });

        Button finish_button = findViewById(R.id.finish_button);

        finish_button.setOnClickListener(v -> {
            DatabaseReference ref = root.push();
            order.setOrder_id(ref.getKey());
            ref.setValue(order);
            Toast.makeText(ChooseBusinessOwnerActivity.this, "Order successfully created!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng chosenLocation = new LatLng(userDetailsList.get(0).getLatitude(), userDetailsList.get(0).getLongitude());
        googleMap.addMarker(new MarkerOptions().position(chosenLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chosenLocation, 17));
    }
}
