package com.example.fineoutside.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.MessageAdapter;
import com.example.fineoutside.data.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserMessagesActivity extends LocationActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("chat messages");

    private RecyclerView userMessagesList;

    private String postId, userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_messages);
        initAttrs();
        initUI();
        subscribeUI();
    }

    private void initAttrs() {
        postId = getIntent().getStringExtra("Post_id");
        userName = getIntent().getStringExtra("User_name");
    }

    protected void initUI() {
        userMessagesList = findViewById(R.id.userMessagesList);
    }

    private void subscribeUI() {
        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messageList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    if (message != null) {
                        if (message.getPost_id().equalsIgnoreCase(postId) && message.getUser_name().equalsIgnoreCase(userName)) {
                            messageList.add(message);
                        }
                    }
                }
                userMessagesList.setAdapter(new MessageAdapter(messageList, false, false, false));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
