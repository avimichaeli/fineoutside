package com.example.fineoutside.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.UserAdapter;
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

public class BabysitterProfileActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");

    private SearchView search_users;
    private RecyclerView users_list;
    private TextView no_users_message_text;

    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<UserDetails> userDetailsList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                UserDetails userDetails = dataSnapshot.getValue(UserDetails.class);
                if (userDetails != null && user != null) {
                    if (userDetails.getUserType().equalsIgnoreCase("Regular") && userDetails.getSelectedBabysitterId() != null && userDetails.getSelectedBabysitterId().equalsIgnoreCase(user.getUid())) {
                        userDetailsList.add(userDetails);
                    }
                }
            }
            if (userDetailsList.isEmpty()) {
                search_users.setVisibility(View.GONE);
                users_list.setVisibility(View.GONE);
                no_users_message_text.setVisibility(View.VISIBLE);
            } else {
                search_users.setVisibility(View.VISIBLE);
                users_list.setVisibility(View.VISIBLE);
                no_users_message_text.setVisibility(View.GONE);
            }
            users_list.setAdapter(new UserAdapter(userDetailsList, (uid, user_name) -> {
                if (user != null) {
                    Intent basicChatIntent = new Intent(BabysitterProfileActivity.this, BasicChatActivity.class);
                    basicChatIntent.putExtra("MY_UID", user.getUid());
                    basicChatIntent.putExtra("OTHER_UID", uid);
                    basicChatIntent.putExtra("MY_USER_NAME", user.getDisplayName());
                    basicChatIntent.putExtra("OTHER_USER_NAME", user_name);
                    basicChatIntent.putExtra("ROOT", "babysitter chat messages");
                    basicChatIntent.putExtra("STORAGE", "babysitter_chat_pics");
                    startActivity(basicChatIntent);
                }
            }));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_babysitter_profile);
        initUI();
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

    protected void initUI() {
        super.initUI();
        search_users = findViewById(R.id.search_users);
        users_list = findViewById(R.id.users_list);
        no_users_message_text = findViewById(R.id.no_users_message_text);
    }

    private void initListeners() {
        search_users.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                UserAdapter userAdapter = (UserAdapter) users_list.getAdapter();
                if (userAdapter != null) {
                    userAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }
}
