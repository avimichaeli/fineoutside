package com.example.fineoutside.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.fineoutside.adapters.FamilyProfileChatAdapter;
import com.example.fineoutside.data.BasicChat;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BasicChatActivity extends LocationActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private DatabaseReference root;
    private StorageReference storageRef;

    private TextView chat_title;
    private RecyclerView chatList;
    private ImageButton add_photo_button, locationButton;
    private final ActivityResultLauncher<Void> takePicture = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
        ref.putBytes(data).addOnCompleteListener(task -> pic = ref.getPath());
        Glide.with(this).load(result).circleCrop().into(add_photo_button);
    });
    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
        ref.putFile(result).addOnCompleteListener(task -> pic = ref.getPath());
        Glide.with(this).load(result).circleCrop().into(add_photo_button);
    });
    private EditText write_your_message_edit;
    private Button send_button;
    private String my_uid;
    private String other_uid;
    private String my_user_name;
    private String other_user_name;
    private String pic;
    private final String current_user_name = user == null ? "" : (user.getDisplayName() == null ? "" : user.getDisplayName());
    private final ValueEventListener chatListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<BasicChat> basicChats = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                BasicChat basicChat = dataSnapshot.getValue(BasicChat.class);
                if (basicChat != null && ((basicChat.getMy_uid().equalsIgnoreCase(other_uid) && basicChat.getOther_uid().equalsIgnoreCase(my_uid)) || (basicChat.getMy_uid().equalsIgnoreCase(my_uid) && basicChat.getOther_uid().equalsIgnoreCase(other_uid)))) {
                    basicChats.add(basicChat);
                    if (current_user_name.equalsIgnoreCase(basicChat.getMy_user_name())) {
                        basicChat.setSeen_by_my_user(true);
                    } else if (current_user_name.equalsIgnoreCase(basicChat.getOther_user_name())) {
                        basicChat.setSeen_by_other_user(true);
                    }
                    dataSnapshot.getRef().setValue(basicChat);
                }
            }

            chatList.setAdapter(new FamilyProfileChatAdapter(basicChats));

            // Scrolling to the last element of the list //
            chatList.scrollToPosition(basicChats.size() - 1);
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_chat);
        initAttrs();
        initUI();
        fillDetails();
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        root.addValueEventListener(chatListener);
    }

    @Override
    protected void onStop() {
        root.removeEventListener(chatListener);
        super.onStop();
    }

    private void initAttrs() {
        my_uid = getIntent().getStringExtra("MY_UID");
        other_uid = getIntent().getStringExtra("OTHER_UID");
        my_user_name = getIntent().getStringExtra("MY_USER_NAME");
        other_user_name = getIntent().getStringExtra("OTHER_USER_NAME");
        String rootText = getIntent().getStringExtra("ROOT");
        String storageText = getIntent().getStringExtra("STORAGE");

        root = database.getReference().child(rootText);
        storageRef = storage.getReference(storageText);

        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    protected void initUI() {
        chat_title = findViewById(R.id.chat_title);
        chatList = findViewById(R.id.chatList);
        add_photo_button = findViewById(R.id.add_photo_button);
        write_your_message_edit = findViewById(R.id.write_your_message_edit);
        send_button = findViewById(R.id.send_button);
        locationButton = findViewById(R.id.location_button);
    }

    private void fillDetails() {
        chat_title.setText(getString(R.string.users_chat_title, my_user_name, other_user_name));
    }

    private void initListeners() {
        send_button.setOnClickListener(v -> {
            String message = write_your_message_edit.getText().toString();
            if (user.getDisplayName() != null && !message.trim().isEmpty()) {
                HashMap<String, String> chat = new HashMap<>();
                chat.put("my_uid", my_uid);
                chat.put("other_uid", other_uid);
                chat.put("my_user_name", user.getDisplayName());
                chat.put("other_user_name", user.getDisplayName().equalsIgnoreCase(other_user_name) ? my_user_name : other_user_name);
                chat.put("message", message);
                chat.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(System.currentTimeMillis()));
                chat.put("pic", pic);
                chat.put("profile_pic", user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
                chat.put("latitude", userLocation.getLatitude() + "");
                chat.put("longitude", userLocation.getLongitude() + "");
                root.push().setValue(chat);
                // Clear the edit text after creating new message //
                write_your_message_edit.setText("");
                // Clear the add photo button after creating new message //
                add_photo_button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_add_photo));
                pic = null;
            }
        });
        add_photo_button.setOnClickListener(v -> new AlertDialog.Builder(this)
            .setTitle(R.string.add_photo)
            .setMessage(R.string.select_option)
            .setPositiveButton(R.string.camera, (dialog, which) -> takePicture.launch(null))
            .setNegativeButton(R.string.photo_library, (dialog, which) -> pickImage.launch("image/*"))
            .setNeutralButton(R.string.cancel, null)
            .create()
            .show());
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
    }

    private void shareCurrentLocation() {
        if (userLocation != null) {
            HashMap<String, String> familyProfileChat = new HashMap<>();
            familyProfileChat.put("my_uid", my_uid);
            familyProfileChat.put("other_uid", other_uid);
            familyProfileChat.put("my_user_name", user.getDisplayName());
            familyProfileChat.put("other_user_name", user.getDisplayName() == null ? "" : user.getDisplayName().equalsIgnoreCase(other_user_name) ? my_user_name : other_user_name);
            familyProfileChat.put("message", "");
            familyProfileChat.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(System.currentTimeMillis()));
            familyProfileChat.put("latitude", userLocation.getLatitude() + "");
            familyProfileChat.put("longitude", userLocation.getLongitude() + "");
            root.push().setValue(familyProfileChat);
        } else {
            Toast.makeText(this, R.string.cant_find_location, Toast.LENGTH_SHORT).show();
        }
    }
}
