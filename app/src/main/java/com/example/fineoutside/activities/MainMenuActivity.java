package com.example.fineoutside.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.fineoutside.R;
import com.example.fineoutside.data.BasicChat;
import com.example.fineoutside.data.Message;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressLint("UnsafeExperimentalUsageError")
public class MainMenuActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference parents_community_help_root = database.getReference().child("community messages");
    private final DatabaseReference emergency_root = database.getReference().child("emergency messages");
    private final DatabaseReference family_profile_chat_root = database.getReference().child("family profile chat messages");
    private final DatabaseReference babysitter_chat_root = database.getReference().child("babysitter chat messages");

    ImageView image_meeting_new_people_main_menu;
    TextView text_meeting_new_people_main_menu;

    ImageView image_whiteboard_main_menu;
    TextView text_whiteboard_main_menu;

    ImageView image_food_delivery_main_menu;
    TextView text_food_deliver_main_menu;

    ImageView image_parent_community_help_main_menu;
    TextView text_parent_community_help_main_menu;

    ImageView image_babysitting_services_main_menu;
    TextView text_babysitting_services_main_menu;

    ImageView image_emergency_center_main_menu;
    TextView text_emergency_center_main_menu;

    ImageView image_groups_main_menu;
    TextView text_groups_main_menu;
    ImageView logo_image;
    private BadgeDrawable badgeDrawable;
    private Location userLocation;
    private final ValueEventListener parentsCommunityHelpOrEmergencyListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Message> messages = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null && userLocation != null) {
                    message.setId(dataSnapshot.getKey());
                    if (System.currentTimeMillis() - Long.parseLong(message.getTime_in_millis()) >= TimeUnit.HOURS.toMillis(2)) {
                        message.setStatus(false);
                        dataSnapshot.getRef().setValue(message);
                    }
                    if (message.getPost_latitude() != null && message.getPost_longitude() != null) {
                        Location messageLocation = new Location("Message_location");
                        messageLocation.setLatitude(Double.parseDouble(message.getPost_latitude()));
                        messageLocation.setLongitude(Double.parseDouble(message.getPost_longitude()));
                        if (userLocation.distanceTo(messageLocation) <= 300 && message.getStatus() == null && System.currentTimeMillis() - Long.parseLong(message.getTime_in_millis()) <= TimeUnit.DAYS.toMillis(1)) {
                            messages.add(message);
                        }
                    }
                }
            }
            int unseenCount = 0;
            for (Message message : messages) {
                if (message.getUnseen_count() != 0 && message.getUser_name().equalsIgnoreCase(user == null ? "" : user.getDisplayName())) {
                    unseenCount += message.getUnseen_count();
                }
                if (!message.getSeen_users().contains(user == null ? "" : user.getUid())) {
                    unseenCount++;
                }
            }
            String key = snapshot.getKey();
            boolean isEmergency = key == null || key.contains("emergency");
            View badgeView = isEmergency ? image_emergency_center_main_menu : image_parent_community_help_main_menu;
            if (unseenCount != 0 && snapshot.getKey() != null) {
                badgeDrawable.setNumber(unseenCount);
                BadgeUtils.attachBadgeDrawable(badgeDrawable, badgeView);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),
                    isEmergency ? Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.emergency) :
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                ringtone.play();
            } else {
                BadgeUtils.detachBadgeDrawable(badgeDrawable, badgeView);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private final ValueEventListener familyProfileChatListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            int unseenCount = 0;
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                BasicChat basicChat = dataSnapshot.getValue(BasicChat.class);
                if (basicChat != null && userLocation != null) {
                    if (basicChat.getLatitude() != null && basicChat.getLongitude() != null) {
                        Location chatLocation = new Location("Chat_location");
                        chatLocation.setLatitude(Double.parseDouble(basicChat.getLatitude()));
                        chatLocation.setLongitude(Double.parseDouble(basicChat.getLongitude()));
                        if (user != null && (userLocation.distanceTo(chatLocation) <= 2000 && ((user.getDisplayName() == null ? "" : user.getDisplayName()).equalsIgnoreCase(basicChat.getMy_user_name()) && !basicChat.isSeen_by_my_user()) || ((user.getDisplayName() == null ? "" : user.getDisplayName()).equalsIgnoreCase(basicChat.getOther_user_name()) && !basicChat.isSeen_by_other_user()))) {
                            unseenCount++;
                        }
                    }
                }
            }

            View badgeView = image_meeting_new_people_main_menu;
            addOrRemoveBadge(unseenCount, badgeView);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private final ValueEventListener babysitterChatListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            int unseenCount = 0;
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                BasicChat basicChat = dataSnapshot.getValue(BasicChat.class);
                if (basicChat != null && userLocation != null) {
                    if (basicChat.getLatitude() != null && basicChat.getLongitude() != null) {
                        Location chatLocation = new Location("Chat_location");
                        chatLocation.setLatitude(Double.parseDouble(basicChat.getLatitude()));
                        chatLocation.setLongitude(Double.parseDouble(basicChat.getLongitude()));
                        if (user != null && (userLocation.distanceTo(chatLocation) <= 2000 && ((user.getDisplayName() == null ? "" : user.getDisplayName()).equalsIgnoreCase(basicChat.getMy_user_name()) && !basicChat.isSeen_by_my_user()) || ((user.getDisplayName() == null ? "" : user.getDisplayName()).equalsIgnoreCase(basicChat.getOther_user_name()) && !basicChat.isSeen_by_other_user()))) {
                            unseenCount++;
                        }
                    }
                }
            }

            View badgeView = image_babysitting_services_main_menu;
            addOrRemoveBadge(unseenCount, badgeView);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void addOrRemoveBadge(int unseenCount, View badgeView) {
        if (unseenCount != 0) {
            badgeDrawable.setNumber(unseenCount);
            BadgeUtils.attachBadgeDrawable(badgeDrawable, badgeView);
        } else {
            BadgeUtils.detachBadgeDrawable(badgeDrawable, badgeView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        super.initUI();

        image_meeting_new_people_main_menu = findViewById(R.id.meeting_new_people_main_menu_image);
        text_meeting_new_people_main_menu = findViewById(R.id.meeting_new_people_main_menu_text);

        image_whiteboard_main_menu = findViewById(R.id.whiteboard_image);
        text_whiteboard_main_menu = findViewById(R.id.whiteboard_text);

        image_food_delivery_main_menu = findViewById(R.id.food_delivery_image);
        text_food_deliver_main_menu = findViewById(R.id.food_delivery_text);

        image_parent_community_help_main_menu = findViewById(R.id.parent_community_help_image);
        text_parent_community_help_main_menu = findViewById(R.id.parent_community_help_text);

        image_babysitting_services_main_menu = findViewById(R.id.babysitting_service_image);
        text_babysitting_services_main_menu = findViewById(R.id.babysitting_service_text);

        image_emergency_center_main_menu = findViewById(R.id.emergency_center_image);
        text_emergency_center_main_menu = findViewById(R.id.emergency_center_text);

        image_groups_main_menu = findViewById(R.id.groups_image);
        text_groups_main_menu = findViewById(R.id.groups_text);

        image_meeting_new_people_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, MeetingNewPeopleActivity.class)));
        text_meeting_new_people_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, MeetingNewPeopleActivity.class)));

        image_whiteboard_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, WhiteBoardActivity.class)));
        text_whiteboard_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, WhiteBoardActivity.class)));

        image_food_delivery_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, FoodDeliveryActivity.class)));
        text_food_deliver_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, FoodDeliveryActivity.class)));

        image_parent_community_help_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, ParentCommunityHelpOrEmergencyCenterActivity.class)));
        text_parent_community_help_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, ParentCommunityHelpOrEmergencyCenterActivity.class)));

        image_babysitting_services_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, BabysittingServicesActivity.class)));
        text_babysitting_services_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, BabysittingServicesActivity.class)));

        image_emergency_center_main_menu.setOnClickListener(v -> {
            Intent emergencyIntent = new Intent(MainMenuActivity.this, ParentCommunityHelpOrEmergencyCenterActivity.class);
            emergencyIntent.putExtra("Emergency", true);
            startActivity(emergencyIntent);
        });
        text_emergency_center_main_menu.setOnClickListener(v -> {
            Intent emergencyIntent = new Intent(MainMenuActivity.this, ParentCommunityHelpOrEmergencyCenterActivity.class);
            emergencyIntent.putExtra("Emergency", true);
            startActivity(emergencyIntent);
        });

        image_groups_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, GroupsActivity.class)));
        text_groups_main_menu.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, GroupsActivity.class)));

        logo_image = findViewById(R.id.logo_image);

        logo_image.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, AnalyticsActivity.class)));

        badgeDrawable = BadgeDrawable.create(MainMenuActivity.this);

        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        SharedPreferences userEntrance = getSharedPreferences("USER_ENTRANCE", MODE_PRIVATE);
        userEntrance.edit().putInt(user.getUid(), userEntrance.getInt(user.getUid(), 0) + 1).apply();

        int userEntranceNumber = userEntrance.getInt(user.getUid(), 0);

        if (userEntranceNumber == 1000) {
            new AlertDialog.Builder(this).setMessage(R.string.price).setNegativeButton("Ok", null).create().show();
        }

        if (userEntranceNumber % 20 == 0) {

            List<String> list = Arrays.asList("WhiteBoard", "MeetingNewPeople", "ParentCommunityHelp", "FoodDelivery", "Groups", "BabysittingServices", "EmergencyCenter");

            Map<String, Integer> map = new HashMap<>();

            for (String feature : list) {
                map.put(feature, getSharedPreferences(feature, MODE_PRIVATE).getInt(user.getUid(), 0));
            }

            Map.Entry<String, Integer> minEntry = null;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (minEntry == null || minEntry.getValue() > entry.getValue()) {
                    minEntry = entry;
                }
            }

            if (minEntry != null) {
                new AlertDialog.Builder(MainMenuActivity.this).setMessage("You should try the " + minEntry.getKey() + " feature more").setNegativeButton("Ok", null).create().show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        parents_community_help_root.addValueEventListener(parentsCommunityHelpOrEmergencyListener);
        emergency_root.addValueEventListener(parentsCommunityHelpOrEmergencyListener);
        family_profile_chat_root.addValueEventListener(familyProfileChatListener);
        babysitter_chat_root.addValueEventListener(babysitterChatListener);
    }

    @Override
    protected void onStop() {
        parents_community_help_root.removeEventListener(parentsCommunityHelpOrEmergencyListener);
        emergency_root.removeEventListener(parentsCommunityHelpOrEmergencyListener);
        family_profile_chat_root.removeEventListener(familyProfileChatListener);
        babysitter_chat_root.removeEventListener(babysitterChatListener);
        super.onStop();
    }
}
