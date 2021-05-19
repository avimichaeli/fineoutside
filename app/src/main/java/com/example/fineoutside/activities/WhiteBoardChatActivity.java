package com.example.fineoutside.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.adapters.WhiteBoardPostChatAdapter;
import com.example.fineoutside.data.WhiteBoardPostChat;
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

public class WhiteBoardChatActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("white board posts chats");
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference("white_board_posts_chats_pics");
    private View first_message_container;
    private RecyclerView chatsList;
    private ImageButton add_photo_button;
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
    private String post_id, post_subject, post_content, post_groups, pic;
    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<WhiteBoardPostChat> whiteBoardPostChatList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                WhiteBoardPostChat whiteBoardPostChat = dataSnapshot.getValue(WhiteBoardPostChat.class);
                if (whiteBoardPostChat != null && whiteBoardPostChat.getPost_id().equalsIgnoreCase(post_id)) {
                    whiteBoardPostChatList.add(whiteBoardPostChat);
                }
                chatsList.setAdapter(new WhiteBoardPostChatAdapter(whiteBoardPostChatList));

                // Scrolling to the last element of the list //
                chatsList.scrollToPosition(whiteBoardPostChatList.size() - 1);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board_chat);
        initAttrs();
        initUI();
        fillDetails();
        initListeners();
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
        post_id = getIntent().getStringExtra("Post_id");
        post_subject = getIntent().getStringExtra("Post_subject");
        post_content = getIntent().getStringExtra("Post_content");
        post_groups = getIntent().getStringExtra("Post_groups");
    }

    protected void initUI() {
        first_message_container = findViewById(R.id.first_message_container);
        chatsList = findViewById(R.id.chatsList);
        add_photo_button = findViewById(R.id.add_photo_button);
        write_your_message_edit = findViewById(R.id.write_your_message_edit);
        send_button = findViewById(R.id.send_button);
    }

    private void fillDetails() {
        first_message_container.setBackgroundResource(android.R.color.holo_blue_light);
        first_message_container.findViewById(R.id.delete_post_button).setVisibility(View.GONE);
        ((TextView) first_message_container.findViewById(R.id.post_subject_text)).setText(post_subject);
        ((TextView) first_message_container.findViewById(R.id.post_content_text)).setText(post_content);
        ((TextView) first_message_container.findViewById(R.id.groups_names_text)).setText(post_groups);
    }

    private void initListeners() {
        send_button.setOnClickListener(v -> {
            String message = write_your_message_edit.getText().toString();
            if (!message.trim().isEmpty()) {
                HashMap<String, String> whiteBoardChat = new HashMap<>();
                whiteBoardChat.put("post_id", post_id);
                whiteBoardChat.put("message", message);
                whiteBoardChat.put("user_name", user.getDisplayName());
                whiteBoardChat.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(System.currentTimeMillis()));
                whiteBoardChat.put("pic", pic);
                root.push().setValue(whiteBoardChat);
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
    }
}
