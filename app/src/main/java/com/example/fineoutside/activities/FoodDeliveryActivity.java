package com.example.fineoutside.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.OrderItemsAdapter;
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
import java.util.Arrays;
import java.util.List;

public class FoodDeliveryActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRoot = database.getReference().child("fine outside users");
    private final List<OrderItem> childrenOrderItemsList = Arrays.asList(
        new OrderItem("Hamburger", "A hamburger is a sandwich consisting of one or more cooked patties of ground meat, usually beef, placed inside a sliced bread roll or bun.", 0, R.drawable.hamburger, 30.0),
        new OrderItem("Pizza", "Pizza is a savory dish of Italian origin consisting of a usually round, flattened base of leavened wheat-based dough topped with tomatoes, cheese, and often various other ingredients, which is then baked at a high temperature, traditionally in a wood-fired oven.", 0, R.drawable.pizza, 15.0),
        new OrderItem("Sushi", "Sushi is a traditional Japanese dish of prepared vinegared rice, usually with some sugar and salt, accompanying a variety of ingredients, such as seafood, often raw, and vegetables.", 0, R.drawable.sushi, 45.0),
        new OrderItem("Schnitzel and Chips", "A schnitzel is a thin slice of meat fried in fat. The meat is usually thinned by pounding with a meat tenderizer.", 0, R.drawable.schnizel, 60.0));
    private final List<OrderItem> parentsOrderItemsList = Arrays.asList(
        new OrderItem("Black Coffee", "Coffee is a brewed drink prepared from roasted coffee beans, the seeds of berries from certain coffee species.", 0, R.drawable.black_coffee, 5.0),
        new OrderItem("Cappuccino", "A cappuccino is an espresso-based coffee drink that originated in Italy, and is traditionally prepared with steamed milk foam.", 0, R.drawable.cappuccino, 7.0),
        new OrderItem("Tea", "Tea is an aromatic beverage prepared by pouring hot or boiling water over cured or fresh leaves of Camellia sinensis, an evergreen shrub native to China and East Asia.", 0, R.drawable.tea, 9.0));
    private Spinner order_type_spinner;
    private SearchView search_order_items;
    private RecyclerView order_items_list;
    private EditText phone_number_edit;
    private Button finish_order_button;
    private String foodType = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_delivery);
        initUI();
        initListeners();

        SharedPreferences userEntrance = getSharedPreferences("FoodDelivery", MODE_PRIVATE);
        userEntrance.edit().putInt(user.getUid(), userEntrance.getInt(user.getUid(), 0) + 1).apply();
    }

    @Override
    protected int getFeatureImage() {
        return R.drawable.food_delivery;
    }

    protected void initUI() {
        super.initUI();
        order_type_spinner = findViewById(R.id.order_type_spinner);
        search_order_items = findViewById(R.id.search_order_items);
        order_items_list = findViewById(R.id.order_items_list);
        phone_number_edit = findViewById(R.id.phone_number_edit);
        finish_order_button = findViewById(R.id.finish_order_button);
    }

    private void initListeners() {
        order_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                order_items_list.setAdapter(new OrderItemsAdapter(position == 0 ? childrenOrderItemsList : parentsOrderItemsList));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        search_order_items.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                OrderItemsAdapter orderItemsAdapter = (OrderItemsAdapter) order_items_list.getAdapter();
                if (orderItemsAdapter != null) {
                    orderItemsAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        finish_order_button.setOnClickListener(v -> {
            OrderItemsAdapter orderItemsAdapter = ((OrderItemsAdapter) order_items_list.getAdapter());
            if (orderItemsAdapter != null) {
                List<OrderItem> orderItemList = orderItemsAdapter.getOrderItemsList();
                boolean isValidOrder = false;
                for (OrderItem orderItem : orderItemList) {
                    if (orderItem.getQuantity() > 0) {
                        isValidOrder = true;
                        foodType = orderItem.getName();
                        break;
                    }
                }
                if (!isValidOrder) {
                    Toast.makeText(FoodDeliveryActivity.this, "You should select 1 of the items from the list", Toast.LENGTH_SHORT).show();
                    return;
                }
                int zeroCount = 0;
                for (OrderItem orderItem : orderItemList) {
                    if (orderItem.getQuantity() == 0) zeroCount++;
                }
                if (zeroCount != orderItemList.size() - 1) {
                    Toast.makeText(FoodDeliveryActivity.this, "You should select only 1 type of food", Toast.LENGTH_SHORT).show();
                    return;
                }
                String phoneNumber = phone_number_edit.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(this, "You should insert a valid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
                double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
                double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
                Location userLocation = new Location("User_location");
                userLocation.setLatitude(latitude);
                userLocation.setLongitude(longitude);
                usersRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<UserDetails> userDetailsList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                            if (userDetails != null && userDetails.getUserType().equalsIgnoreCase("Business Owner") && userDetails.getFoodType().equalsIgnoreCase(foodType)) {
                                Location businessOwnerLocation = new Location("Business_owner_location");
                                businessOwnerLocation.setLatitude(userDetails.getLatitude());
                                businessOwnerLocation.setLongitude(userDetails.getLongitude());
                                if (userLocation.distanceTo(businessOwnerLocation) <= 5000) {
                                    userDetailsList.add(userDetails);
                                }
                            }
                        }
                        if (userDetailsList.isEmpty()) {
                            Toast.makeText(FoodDeliveryActivity.this, "There is no business for your food order, please try another food type", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent chooseBusinessOwnerIntent = new Intent(FoodDeliveryActivity.this, ChooseBusinessOwnerActivity.class);
                            chooseBusinessOwnerIntent.putParcelableArrayListExtra("UserDetailsList", (ArrayList<? extends Parcelable>) userDetailsList);
                            chooseBusinessOwnerIntent.putExtra("Order", new Order("", user.getUid(), latitude, longitude, orderItemList, phoneNumber, null));
                            startActivity(chooseBusinessOwnerIntent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}
