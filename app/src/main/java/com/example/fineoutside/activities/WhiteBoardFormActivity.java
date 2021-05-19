package com.example.fineoutside.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fineoutside.R;
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
import java.util.List;

public class WhiteBoardFormActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference groupsRoot = database.getReference().child("groups");
    private final DatabaseReference root = database.getReference().child("white board posts");
    private final List<String> groups_names = new ArrayList<>();
    private EditText post_subject_edit, post_content_edit;
    private ListView groupNamesList;
    private final ValueEventListener valueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            List<String> groupList = new ArrayList<>();
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                Group group = dataSnapshot.getValue(Group.class);
                if (group != null && user != null) {
                    if (group.getRegistered_users() != null && group.getRegistered_users().contains(user.getUid())) {
                        groupList.add(group.getName());
                    }
                }
            }
            groupNamesList.setAdapter(new ArrayAdapter<String>(WhiteBoardFormActivity.this, android.R.layout.select_dialog_multichoice, groupList) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    CheckedTextView checkedTextView = (CheckedTextView) super.getView(position, convertView, parent);
                    checkedTextView.setOnClickListener(v -> {
                        checkedTextView.setChecked(!checkedTextView.isChecked());
                        String item = groupList.get(position);
                        if (groups_names.contains(item)) {
                            groups_names.remove(item);
                        } else {
                            groups_names.add(item);
                        }
                    });
                    return checkedTextView;
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private Button finish_button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board_form);
        initUI();
        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupsRoot.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        groupsRoot.removeEventListener(valueEventListener);
    }

    protected void initUI() {
        post_subject_edit = findViewById(R.id.post_subject_edit);
        post_content_edit = findViewById(R.id.post_content_edit);
        groupNamesList = findViewById(R.id.groupNamesList);
        finish_button = findViewById(R.id.finish_button);
    }

    private void initListeners() {
        finish_button.setOnClickListener(v -> {
            String postSubject = post_subject_edit.getText().toString();
            if (postSubject.trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid post subject", Toast.LENGTH_SHORT).show();
                return;
            }
            String postContent = post_content_edit.getText().toString();
            if (postContent.trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid post content", Toast.LENGTH_SHORT).show();
                return;
            }
            if (groups_names.isEmpty()) {
                Toast.makeText(this, "Please select at least 1 group", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseReference reference = root.push();
            reference.setValue(new WhiteBoardPost(reference.getKey(), postSubject, postContent, user.getUid(), groups_names, System.currentTimeMillis()));
            Toast.makeText(WhiteBoardFormActivity.this, "The post has successfully created", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
