package com.example.fineoutside.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.adapters.MessageAdapter;
import com.example.fineoutside.data.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ParentCommunityOrEmergencyCenterChatActivity extends LocationActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("chat messages");
    private final DatabaseReference community_root = database.getReference().child("community messages");
    private final DatabaseReference emergency_root = database.getReference().child("emergency messages");
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference("chat_pics");
    private TextView userName, firstMessage, time, watchesCounter, thatIsDone, iDidNotGetHelp;
    private ImageView profilePic;
    private RecyclerView chatsList;
    private ImageButton locationButton, addPhoto;
    private EditText writeYourMessage;
    private Button send;
    private String id, post_user_name, first_message, current_user_name, time_text, pic, profile_pic, watches_counter, post_latitude, post_longitude;
    private boolean status;
    private final ValueEventListener rootListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Message> messages = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Message message = dataSnapshot.getValue(Message.class);
                // Checking if the accepted or the sent message is relevant for this chat //
                if (message != null && message.getPost_id().equalsIgnoreCase(id)) {
                    messages.add(message);
                    if (message.getSeen_by_owner() == null || message.getSeen_by_owner().equalsIgnoreCase("false")) {
                        message.setSeen_by_owner(String.valueOf(current_user_name.equalsIgnoreCase(post_user_name)));
                        dataSnapshot.getRef().setValue(message);
                    }
                }
            }

            for (Message message : messages) {
                if (message.getSeen_by_owner() != null && message.getSeen_by_owner().equalsIgnoreCase("false")) {
                    message.setUnseen_count(message.getUnseen_count() + 1);
                }
            }
            if ((post_user_name.equalsIgnoreCase(current_user_name) && messages.isEmpty()) || !status) {
                writeYourMessage.setVisibility(View.GONE);
                send.setVisibility(View.GONE);
                locationButton.setVisibility(View.GONE);
                addPhoto.setVisibility(View.GONE);
            } else {
                writeYourMessage.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
                locationButton.setVisibility(View.VISIBLE);
                addPhoto.setVisibility(View.VISIBLE);
            }
            chatsList.setAdapter(new MessageAdapter(messages, false, true, false));

            // Scrolling to the last element of the list //
            chatsList.scrollToPosition(messages.size() - 1);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private final ValueEventListener chatListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String user_id = user == null ? "" : user.getUid();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null && message.getId().equalsIgnoreCase(id)) {
                    List<String> watchesList = message.getWatches();
                    if (!watchesList.contains(user_id)) watchesList.add(user_id);
                    message.setWatches(watchesList);
                    if (current_user_name.equalsIgnoreCase(post_user_name)) {
                        message.setUnseen_count(0);
                    }
                    watchesCounter.setText(String.valueOf(watchesList.size()));
                    dataSnapshot.getRef().setValue(message);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private final ValueEventListener unseenListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Message message = snapshot.getValue(Message.class);
            if (message != null) {
                message.setUnseen_count(current_user_name.equalsIgnoreCase(post_user_name) ? 0 : message.getUnseen_count() + 1);
                snapshot.getRef().setValue(message);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private Location userLocation;
    private final ActivityResultLauncher<String> permissionRequest =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) shareCurrentLocation();
            else Toast.makeText(this, R.string.need_location_permission, Toast.LENGTH_LONG).show();
        });
    private final ActivityResultLauncher<Void> takePicture = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
        ref.putBytes(data).addOnCompleteListener(task -> pic = ref.getPath());
        Glide.with(this).load(result).circleCrop().into(addPhoto);
    });
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
        ref.putFile(result).addOnCompleteListener(task -> pic = ref.getPath());
        Glide.with(this).load(result).circleCrop().into(addPhoto);
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_community_or_emergency_center_chat);
        initAttrs();
        initUI();
        fillDetails();
        initListeners();
    }

    @Override
    protected int getFeatureImage() {
        return R.drawable.parent_community_help;
    }

    @Override
    protected void onStart() {
        super.onStart();
        root.addValueEventListener(rootListener);
        community_root.addValueEventListener(chatListener);
        emergency_root.addValueEventListener(chatListener);
    }

    @Override
    protected void onStop() {
        root.removeEventListener(rootListener);
        community_root.removeEventListener(chatListener);
        emergency_root.removeEventListener(chatListener);
        super.onStop();
    }

    private void initAttrs() {
        // The name of the user we chat with //
        post_user_name = getIntent().getStringExtra("User_name");
        // The message id of the chat //
        id = getIntent().getStringExtra("Id");
        // The first message of the chat //
        first_message = getIntent().getStringExtra("First_message");
        // The first message time of the chat //
        time_text = getIntent().getStringExtra("Time");
        // The user profile pic //
        profile_pic = getIntent().getStringExtra("Profile_pic");
        // The first message watches counter //
        watches_counter = getIntent().getStringExtra("Watches_counter");
        // The chat status //
        status = getIntent().getBooleanExtra("Status", false);
        // The first message latitude //
        post_latitude = getIntent().getStringExtra("Post_latitude");
        // The first message longitude //
        post_longitude = getIntent().getStringExtra("Post_longitude");

        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    protected void initUI() {
        super.initUI();
        View firstMessageContainer = findViewById(R.id.first_message_container);
        firstMessageContainer.setBackgroundResource(android.R.color.holo_blue_light);
        userName = firstMessageContainer.findViewById(R.id.user_name_text);
        firstMessage = firstMessageContainer.findViewById(R.id.message_text);
        time = firstMessageContainer.findViewById(R.id.time_text);
        TextView distance = firstMessageContainer.findViewById(R.id.distance_text);
        distance.setVisibility(View.GONE);
        profilePic = firstMessageContainer.findViewById(R.id.profile_pic_image);
        watchesCounter = firstMessageContainer.findViewById(R.id.watches_counter_text);
        chatsList = findViewById(R.id.chatsList);
        thatIsDone = findViewById(R.id.that_is_done_text);
        iDidNotGetHelp = findViewById(R.id.i_did_not_get_help_text);
        addPhoto = findViewById(R.id.add_photo_button);
        writeYourMessage = findViewById(R.id.write_your_message_edit);
        locationButton = findViewById(R.id.location_button);
        send = findViewById(R.id.send_button);
    }

    private void fillDetails() {
        userName.setText(new StringBuilder(post_user_name + " said:"));
        firstMessage.setText(first_message);
        time.setText(time_text);
        watchesCounter.setText(watches_counter);

        if (profile_pic != null && !profile_pic.isEmpty()) {
            if (profile_pic.startsWith("http")) {
                Glide.with(this).load(profile_pic).circleCrop().into(profilePic);
            } else {
                try {
                    File file = File.createTempFile("images", "jpg");
                    storage.getReference(profile_pic).getFile(file).addOnCompleteListener(task ->
                        Glide.with(this).load(file).circleCrop().into(profilePic));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        current_user_name = user != null ? user.getDisplayName() : "";

        if (post_user_name.equalsIgnoreCase(current_user_name) && status) {
            thatIsDone.setVisibility(View.VISIBLE);
            iDidNotGetHelp.setVisibility(View.VISIBLE);
        } else {
            thatIsDone.setVisibility(View.GONE);
            iDidNotGetHelp.setVisibility(View.GONE);
        }
    }

    private void initListeners() {
        thatIsDone.setOnClickListener(v -> setPostStatus(true));
        iDidNotGetHelp.setOnClickListener(v -> setPostStatus(false));
        locationButton.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle(R.string.sharing_location)
            .setMessage(R.string.are_you_sure_location)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                String permission = Manifest.permission.ACCESS_FINE_LOCATION;
                int permissionStatus = ContextCompat.checkSelfPermission(this, permission);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) shareCurrentLocation();
                else permissionRequest.launch(permission);
            }).create().show());

        addPhoto.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle(R.string.add_photo)
            .setMessage(R.string.select_option)
            .setPositiveButton(R.string.camera, (dialog, which) -> takePicture.launch(null))
            .setNegativeButton(R.string.photo_library, (dialog, which) -> pickImage.launch("image/*"))
            .setNeutralButton(R.string.cancel, null)
            .create()
            .show());

        send.setOnClickListener(v -> {
            String message_content = writeYourMessage.getText().toString();
            if (!message_content.trim().isEmpty()) {
                community_root.child(id).addListenerForSingleValueEvent(unseenListener);
                emergency_root.child(id).addListenerForSingleValueEvent(unseenListener);
                HashMap<String, String> chat = new HashMap<>();
                chat.put("main_subject", first_message);
                chat.put("user_name", current_user_name);
                chat.put("message_content", message_content);
                chat.put("profile_pic", user == null || user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
                chat.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(System.currentTimeMillis()));
                chat.put("time_in_millis", System.currentTimeMillis() + "");
                chat.put("pic", pic);
                if (userLocation != null) {
                    chat.put("latitude", userLocation.getLatitude() + "");
                    chat.put("longitude", userLocation.getLongitude() + "");
                }
                if (post_latitude != null && post_longitude != null) {
                    chat.put("post_latitude", post_latitude);
                    chat.put("post_longitude", post_longitude);
                }
                chat.put("post_id", id);
                chat.put("seen_by_owner", String.valueOf(current_user_name.equalsIgnoreCase(post_user_name)));
                root.push().setValue(chat);
                // Clear the edit text after creating new message //
                writeYourMessage.setText("");
                // Clear the add photo button after creating new message //
                addPhoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_photo));
                pic = null;
            }
        });
    }

    private void shareCurrentLocation() {
        if (userLocation != null) {
            HashMap<String, String> chat = new HashMap<>();
            chat.put("main_subject", first_message);
            chat.put("user_name", current_user_name);
            chat.put("message_content", "");
            chat.put("profile_pic", user == null || user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
            chat.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(System.currentTimeMillis()));
            chat.put("time_in_millis", System.currentTimeMillis() + "");
            chat.put("latitude", userLocation.getLatitude() + "");
            chat.put("longitude", userLocation.getLongitude() + "");
            chat.put("post_id", id);
            root.push().setValue(chat);
        } else {
            Toast.makeText(this, R.string.cant_find_location, Toast.LENGTH_SHORT).show();
        }
    }

    private void setPostStatus(boolean postStatus) {
        community_root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null && message.getId().equalsIgnoreCase(id)) {
                        message.setStatus(postStatus);
                        if (dataSnapshot.getKey() != null) {
                            community_root.child(dataSnapshot.getKey()).setValue(message);
                        }
                        onBackPressed();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        emergency_root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null && message.getId().equalsIgnoreCase(id)) {
                        message.setStatus(postStatus);
                        if (dataSnapshot.getKey() != null) {
                            emergency_root.child(dataSnapshot.getKey()).setValue(message);
                        }
                        onBackPressed();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
