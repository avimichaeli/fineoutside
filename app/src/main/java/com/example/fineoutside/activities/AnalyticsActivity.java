package com.example.fineoutside.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fineoutside.R;
import com.example.fineoutside.data.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class AnalyticsActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");

    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            total_app_users_text.setText(String.valueOf(snapshot.getChildrenCount()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(System.currentTimeMillis()));
            int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
            int currentYear = calendar.get(Calendar.YEAR);
            int count = 0;
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                if (userDetails != null) {
                    calendar.setTime(new Date(userDetails.getJoinedDate()));
                    int createdDateWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                    int createdDateYear = calendar.get(Calendar.YEAR);
                    if (createdDateWeek == currentWeek && createdDateYear == currentYear) {
                        count++;
                    }
                }
            }
            users_joined_count_text.setText(String.valueOf(count));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private TextView user_entrance_text, whiteboard_entrance_text, meeting_new_people_entrance_text, parent_community_help_entrance_text, food_delivery_entrance_text, groups_entrance_text, babysitting_service_entrance_text, emergency_center_entrance_text, total_app_users_text, users_joined_count_text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        initUI();
        fillDetails();
    }

    @Override
    protected void onStart() {
        super.onStart();
        root.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    protected void onStop() {
        root.removeEventListener(valueEventListener);
        super.onStop();
    }

    protected void initUI() {
        user_entrance_text = findViewById(R.id.user_entrance_num_text);
        whiteboard_entrance_text = findViewById(R.id.whiteboard_entrance_num_text);
        meeting_new_people_entrance_text = findViewById(R.id.meeting_new_people_entrance_num_text);
        parent_community_help_entrance_text = findViewById(R.id.parent_community_help_entrance_num_text);
        food_delivery_entrance_text = findViewById(R.id.food_delivery_entrance_num_text);
        groups_entrance_text = findViewById(R.id.groups_entrance_num_text);
        babysitting_service_entrance_text = findViewById(R.id.babysitting_service_entrance_num_text);
        emergency_center_entrance_text = findViewById(R.id.emergency_center_entrance_num_text);
        total_app_users_text = findViewById(R.id.total_app_users_num_text);
        users_joined_count_text = findViewById(R.id.users_joined_count_num_text);
    }

    private void fillDetails() {
        SharedPreferences userEntrance1 = getSharedPreferences("USER_ENTRANCE", MODE_PRIVATE);
        user_entrance_text.setText(String.valueOf(userEntrance1.getInt(user.getUid(), 0)));

        SharedPreferences userEntrance2 = getSharedPreferences("WhiteBoard", MODE_PRIVATE);
        whiteboard_entrance_text.setText(String.valueOf(userEntrance2.getInt(user.getUid(), 0)));

        SharedPreferences userEntrance3 = getSharedPreferences("MeetingNewPeople", MODE_PRIVATE);
        meeting_new_people_entrance_text.setText(String.valueOf(userEntrance3.getInt(user.getUid(), 0)));

        SharedPreferences userEntrance4 = getSharedPreferences("ParentCommunityHelp", MODE_PRIVATE);
        parent_community_help_entrance_text.setText(String.valueOf(userEntrance4.getInt(user.getUid(), 0)));

        SharedPreferences userEntrance5 = getSharedPreferences("FoodDelivery", MODE_PRIVATE);
        food_delivery_entrance_text.setText(String.valueOf(userEntrance5.getInt(user.getUid(), 0)));

        SharedPreferences userEntrance6 = getSharedPreferences("Groups", MODE_PRIVATE);
        groups_entrance_text.setText(String.valueOf(userEntrance6.getInt(user.getUid(), 0)));

        SharedPreferences userEntrance7 = getSharedPreferences("BabysittingServices", MODE_PRIVATE);
        babysitting_service_entrance_text.setText(String.valueOf(userEntrance7.getInt(user.getUid(), 0)));

        SharedPreferences userEntrance8 = getSharedPreferences("EmergencyCenter", MODE_PRIVATE);
        emergency_center_entrance_text.setText(String.valueOf(userEntrance8.getInt(user.getUid(), 0)));
    }
}
