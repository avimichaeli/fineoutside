package com.example.fineoutside.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.adapters.MessageAdapter;
import com.example.fineoutside.data.Message;
import com.example.fineoutside.data.UserDetails;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ParentCommunityHelpOrEmergencyCenterActivity extends LocationActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final DatabaseReference usersRoot = database.getReference().child("fine outside users");
    private final DatabaseReference userRef = usersRoot.child(user == null ? "" : user.getUid());
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference("chat_pics");
    private DatabaseReference root;
    private TextView successCases, failedCases, casesFromNow, enableMessagesText;
    private SwitchCompat enableMessagesSwitch;
    private SearchView searchMessages;
    private RecyclerView messagesList;
    private ImageButton addPhoto;
    private EditText writeYourMessage;
    private Button sendMessage;

    private Boolean status;
    private String pic;
    private final ActivityResultLauncher<Void> takePicture = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
        ref.putBytes(data).addOnCompleteListener(task -> pic = ref.getPath());
        Glide.with(this).load(result).into(addPhoto);
    });
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
        ref.putFile(result).addOnCompleteListener(task -> pic = ref.getPath());
        Glide.with(this).load(result).into(addPhoto);
    });
    private Location userLocation;
    private boolean isEmergency;
    private final ValueEventListener rootListener = new ValueEventListener() {
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
                        if (userLocation.distanceTo(messageLocation) <= 300 && message.getStatus() == status && (status == null || System.currentTimeMillis() - Long.parseLong(message.getTime_in_millis()) <= TimeUnit.DAYS.toMillis(1))) {
                            messages.add(message);
                        }
                    }
                    if (!message.getSeen_users().contains(user == null ? "" : user.getUid())) {
                        message.getSeen_users().add(user == null ? "" : user.getUid());
                        dataSnapshot.getRef().setValue(message);
                    }
                }
            }
            messagesList.setAdapter(new MessageAdapter(messages, true, false, isEmergency));

            // Scrolling to the first element of the list //
            messagesList.scrollToPosition(messages.size() - 1);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_community_help_or_emergency_center);
        initAttrs();
        initUI();
        initListeners();

        SharedPreferences userEntrance = getSharedPreferences(isEmergency ? "EmergencyCenter" : "ParentCommunityHelp", MODE_PRIVATE);
        userEntrance.edit().putInt(user.getUid(), userEntrance.getInt(user.getUid(), 0) + 1).apply();
    }

    @Override
    protected int getFeatureImage() {
        return isEmergency ? R.drawable.emergency : R.drawable.parent_community_help;
    }

    @Override
    protected void onStart() {
        super.onStart();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDetails userDetails = snapshot.getValue(UserDetails.class);
                if (!isEmergency) {
                    if (userDetails != null) {
                        enableMessagesSwitch.setChecked(userDetails.isEnableParentCommunityHelpMessages());
                        findViewById(R.id.message_enable_text).setVisibility(enableMessagesSwitch.isChecked() ? View.GONE : View.VISIBLE);
                    }
                } else {
                    enableMessagesSwitch.setVisibility(View.GONE);
                    enableMessagesSwitch.setChecked(true);
                    enableMessagesText.setText(R.string.emergency_text);
                }
                setMessageList(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStop() {
        root.removeEventListener(rootListener);
        super.onStop();
    }

    private void initAttrs() {
        isEmergency = getIntent().getBooleanExtra("Emergency", false);
        root = database.getReference().child((isEmergency ? "emergency" : "community") + " messages");

        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    protected void initUI() {
        super.initUI();
        successCases = findViewById(R.id.success_cases_text);
        failedCases = findViewById(R.id.failed_cases_text);
        casesFromNow = findViewById(R.id.cases_from_now_text);
        enableMessagesSwitch = findViewById(R.id.enable_messages_switch);
        enableMessagesText = findViewById(R.id.enable_messages_text);
        searchMessages = findViewById(R.id.search_messages);
        messagesList = findViewById(R.id.messagesList);
        addPhoto = findViewById(R.id.add_photo_button);
        writeYourMessage = findViewById(R.id.write_your_message_edit);
        sendMessage = findViewById(R.id.send_message_button);
    }

    private void initListeners() {
        searchMessages.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                MessageAdapter messageAdapter = (MessageAdapter) messagesList.getAdapter();
                if (messageAdapter != null) {
                    messageAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        enableMessagesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            findViewById(R.id.message_enable_text).setVisibility(isChecked ? View.GONE : View.VISIBLE);
            searchMessages.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            messagesList.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            addPhoto.setVisibility(isChecked && status == null ? View.VISIBLE : View.GONE);
            writeYourMessage.setVisibility(isChecked && status == null ? View.VISIBLE : View.GONE);
            sendMessage.setVisibility(isChecked && status == null ? View.VISIBLE : View.GONE);
            if (isEmergency) return;
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserDetails userDetails = snapshot.getValue(UserDetails.class);
                    if (userDetails != null) {
                        userDetails.setEnableParentCommunityHelpMessages(isChecked);
                        userRef.setValue(userDetails);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        addPhoto.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle(R.string.add_photo)
            .setMessage(R.string.select_option)
            .setPositiveButton(R.string.camera, (dialog, which) -> takePicture.launch(null))
            .setNegativeButton(R.string.photo_library, (dialog, which) -> pickImage.launch("image/*"))
            .setNeutralButton(R.string.cancel, null)
            .create()
            .show());

        sendMessage.setOnClickListener(v -> {
            String message_content = writeYourMessage.getText().toString();
            if (!message_content.trim().isEmpty()) {
                Message message = new Message();
                message.setUser_name(user != null ? user.getDisplayName() : "");
                message.setMessage_content(message_content);
                message.setProfile_pic(user == null || user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
                message.setTime(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(System.currentTimeMillis()));
                message.setTime_in_millis(System.currentTimeMillis() + "");
                message.setPic(pic);
                if (userLocation != null) {
                    message.setPost_latitude(userLocation.getLatitude() + "");
                    message.setPost_longitude(userLocation.getLongitude() + "");
                }
                message.setSeen_users(Collections.singletonList(user == null ? "" : user.getUid()));
                DatabaseReference ref = root.push();
                message.setId(ref.getKey());
                ref.setValue(message);
                // Clear the edit text after creating new message //
                writeYourMessage.setText("");
                // Clear the add photo button after creating new message //
                addPhoto.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_photo));
                pic = null;
            }
        });

        successCases.setOnClickListener(v -> {
            setMessageList(true);
            addPhoto.setVisibility(View.GONE);
            writeYourMessage.setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);
        });

        failedCases.setOnClickListener(v -> {
            setMessageList(false);
            addPhoto.setVisibility(View.GONE);
            writeYourMessage.setVisibility(View.GONE);
            sendMessage.setVisibility(View.GONE);
        });

        casesFromNow.setOnClickListener(v -> {
            int visibility = enableMessagesSwitch.isChecked() ? View.VISIBLE : View.GONE;
            addPhoto.setVisibility(visibility);
            writeYourMessage.setVisibility(visibility);
            sendMessage.setVisibility(visibility);
            setMessageList(null);
        });
    }

    private void setMessageList(Boolean status) {
        this.status = status;
        root.addValueEventListener(rootListener);
    }
}
