package com.example.fineoutside.activities;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.OrdersAdapter;
import com.example.fineoutside.data.Order;
import com.example.fineoutside.data.OrderItem;
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

public class BusinessProfileActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");
    private final DatabaseReference ordersRoot = database.getReference().child("food orders");

    private SearchView search_orders;
    private RecyclerView business_orders_list;

    private String businessFoodType;
    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            UserDetails userDetails = snapshot.getValue(UserDetails.class);
            if (userDetails != null) {
                businessFoodType = userDetails.getFoodType();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private Location userLocation;
    private final ValueEventListener ordersValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Order> orderList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Order order = dataSnapshot.getValue(Order.class);
                if (order != null && user != null) {
                    if (order.getTookOrderUserId() != null && order.getTookOrderUserId().equalsIgnoreCase(user.getUid()) && order.getOrderItemList() != null) {
                        boolean foundTheSameFoodType = false;
                        for (OrderItem orderItem : order.getOrderItemList()) {
                            if (orderItem.getQuantity() > 0 && orderItem.getName().equalsIgnoreCase(businessFoodType)) {
                                foundTheSameFoodType = true;
                                break;
                            }
                        }
                        if (foundTheSameFoodType) {
                            Location orderLocation = new Location("Order_location");
                            orderLocation.setLatitude(order.getLatitude());
                            orderLocation.setLongitude(order.getLongitude());
                            order.setDistance((int) userLocation.distanceTo(orderLocation));
                            if (order.getDistance() <= 200) {
                                orderList.add(order);
                            }
                        }
                    }
                }
            }
            for (Order order : orderList) {
                order.setTookOrderUserId(user.getUid());
                ordersRoot.child(order.getOrder_id()).setValue(order);
            }
            business_orders_list.setAdapter(new OrdersAdapter(orderList, true, order -> ordersRoot.child(order.getOrder_id()).setValue(null)));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);
        initUI();
        initAttrs();
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        root.child(user.getUid()).addListenerForSingleValueEvent(valueEventListener);
        ordersRoot.addValueEventListener(ordersValueEventListener);
    }

    @Override
    protected void onStop() {
        root.removeEventListener(valueEventListener);
        ordersRoot.removeEventListener(ordersValueEventListener);
        super.onStop();
    }

    protected void initUI() {
        super.initUI();
        search_orders = findViewById(R.id.search_orders);
        business_orders_list = findViewById(R.id.business_orders_list);
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
        search_orders.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                OrdersAdapter ordersAdapter = (OrdersAdapter) business_orders_list.getAdapter();
                if (ordersAdapter != null) {
                    ordersAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }
}
