package com.example.fineoutside.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fineoutside.R;
import com.example.fineoutside.adapters.WhiteBoardPostAdapter;
import com.example.fineoutside.data.Group;
import com.example.fineoutside.data.WhiteBoardPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WhiteBoardActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference groupsRoot = database.getReference().child("groups");
    private final DatabaseReference root = database.getReference().child("white board posts");
    private final ValueEventListener timeEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                WhiteBoardPost whiteBoardPost = dataSnapshot.getValue(WhiteBoardPost.class);
                if (whiteBoardPost != null) {
                    if (System.currentTimeMillis() - whiteBoardPost.getPosted_time() >= 24 * 60 * 60 * 1000) {
                        dataSnapshot.getRef().setValue(null);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private Button post_whiteboard_message_button;
    private Spinner group_selector_spinner;
    private SearchView search_whiteboard_posts;
    private RecyclerView whiteboardPostsList;
    private TextView no_posts_message_text;
    private Location userLocation;
    private boolean showNoGroupsMessage = true;
    private final ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<Group> groupList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null) {
                    Location groupLocation = new Location("Group_location");
                    groupLocation.setLatitude(Double.parseDouble(group.getLatitude()));
                    groupLocation.setLongitude(Double.parseDouble(group.getLongitude()));
                    if (group.getRegistered_users() != null && group.getRegistered_users().contains(user == null ? "" : user.getUid())) {
                        showNoGroupsMessage = false;
                        if (userLocation.distanceTo(groupLocation) <= 1000) {
                            groupList.add(group);
                        }
                    }
                }
            }

            if (showNoGroupsMessage) {
                return;
            }

            post_whiteboard_message_button.setVisibility(View.VISIBLE);
            group_selector_spinner.setVisibility(View.VISIBLE);
            search_whiteboard_posts.setVisibility(View.VISIBLE);
            whiteboardPostsList.setVisibility(View.VISIBLE);
            no_posts_message_text.setVisibility(View.GONE);

            root.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<WhiteBoardPost> whiteBoardPostList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        WhiteBoardPost whiteBoardPost = dataSnapshot.getValue(WhiteBoardPost.class);
                        if (whiteBoardPost != null) {
                            for (Group group : groupList) {
                                if (!whiteBoardPostList.contains(whiteBoardPost) && whiteBoardPost.getGroups_names().contains(group.getName())) {
                                    whiteBoardPostList.add(whiteBoardPost);
                                }
                            }
                        }
                    }
                    whiteboardPostsList.setAdapter(new WhiteBoardPostAdapter(whiteBoardPostList, postId -> {
                        root.child(postId).setValue(null);
                        Toast.makeText(WhiteBoardActivity.this, "The post has successfully deleted!", Toast.LENGTH_SHORT).show();
                    }));

                    // Scrolling to the first element of the list //
                    whiteboardPostsList.scrollToPosition(whiteBoardPostList.size() - 1);

                    Set<String> groupNames = new HashSet<>();
                    groupNames.add("All");

                    for (WhiteBoardPost whiteBoardPost : whiteBoardPostList) {
                        groupNames.addAll(whiteBoardPost.getGroups_names());
                    }

                    group_selector_spinner.setAdapter(new ArrayAdapter<>(WhiteBoardActivity.this, android.R.layout.simple_spinner_dropdown_item, groupNames.toArray(new String[0])));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board);
        initAttrs();
        initUI();
        initListeners();

        SharedPreferences userEntrance = getSharedPreferences("WhiteBoard", MODE_PRIVATE);
        userEntrance.edit().putInt(user.getUid(), userEntrance.getInt(user.getUid(), 0) + 1).apply();
    }

    @Override
    protected int getFeatureImage() {
        return R.drawable.white_board;
    }

    @Override
    protected void onStart() {
        super.onStart();
        root.addValueEventListener(timeEventListener);
        groupsRoot.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        root.removeEventListener(timeEventListener);
        groupsRoot.removeEventListener(valueEventListener);
        super.onStop();
    }

    private void initAttrs() {
        SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
        double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
        double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
        userLocation = new Location("User_Location");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);
    }

    protected void initUI() {
        super.initUI();
        post_whiteboard_message_button = findViewById(R.id.post_whiteboard_message_button);
        group_selector_spinner = findViewById(R.id.group_selector_spinner);
        search_whiteboard_posts = findViewById(R.id.search_whiteboard_posts);
        whiteboardPostsList = findViewById(R.id.whiteboardMessagesList);
        no_posts_message_text = findViewById(R.id.no_posts_message_text);
    }

    private void initListeners() {
        post_whiteboard_message_button.setOnClickListener(v -> startActivity(new Intent(WhiteBoardActivity.this, WhiteBoardFormActivity.class)));
        group_selector_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                groupsRoot.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Group> groupList = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Group group = dataSnapshot.getValue(Group.class);
                            if (group != null) {
                                Location groupLocation = new Location("Group_location");
                                groupLocation.setLatitude(Double.parseDouble(group.getLatitude()));
                                groupLocation.setLongitude(Double.parseDouble(group.getLongitude()));
                                if (group.getRegistered_users() != null && group.getRegistered_users().contains(user.getUid())) {
                                    showNoGroupsMessage = false;
                                    if (userLocation.distanceTo(groupLocation) <= 1000) {
                                        groupList.add(group);
                                    }
                                }
                            }
                        }

                        if (position != 0) {
                            String groupName = (String) group_selector_spinner.getSelectedItem();
                            Iterator<Group> iterator = groupList.iterator();
                            while (iterator.hasNext()) {
                                Group group = iterator.next();
                                if (!group.getName().equalsIgnoreCase(groupName)) {
                                    iterator.remove();
                                }
                            }
                        }

                        root.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<WhiteBoardPost> whiteBoardPostList = new ArrayList<>();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    WhiteBoardPost whiteBoardPost = dataSnapshot.getValue(WhiteBoardPost.class);
                                    if (whiteBoardPost != null) {
                                        for (Group group : groupList) {
                                            if (!whiteBoardPostList.contains(whiteBoardPost) && whiteBoardPost.getGroups_names().contains(group.getName())) {
                                                whiteBoardPostList.add(whiteBoardPost);
                                            }
                                        }
                                    }
                                }
                                whiteboardPostsList.setAdapter(new WhiteBoardPostAdapter(whiteBoardPostList, postId -> {
                                    root.child(postId).setValue(null);
                                    Toast.makeText(WhiteBoardActivity.this, "The post has successfully deleted!", Toast.LENGTH_SHORT).show();
                                }));

                                // Scrolling to the first element of the list //
                                whiteboardPostsList.scrollToPosition(whiteBoardPostList.size() - 1);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        search_whiteboard_posts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                WhiteBoardPostAdapter whiteBoardPostAdapter = (WhiteBoardPostAdapter) whiteboardPostsList.getAdapter();
                if (whiteBoardPostAdapter != null) {
                    whiteBoardPostAdapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    public interface OnDeleteWhiteBoardPostListener {
        void deletePost(String postId);
    }
}