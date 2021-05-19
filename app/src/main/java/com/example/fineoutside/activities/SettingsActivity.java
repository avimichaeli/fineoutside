package com.example.fineoutside.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fineoutside.R;
import com.example.fineoutside.data.FamilyProfile;
import com.example.fineoutside.data.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");

    private EditText display_name_edit, password_edit;
    private Button change_display_name_button, change_password_button, delete_profile_button, continue_button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initUI();
        fillDetails();
        initListeners();
    }

    protected void initUI() {
        display_name_edit = findViewById(R.id.display_name_edit);
        password_edit = findViewById(R.id.password_edit);
        change_display_name_button = findViewById(R.id.change_display_name_button);
        change_password_button = findViewById(R.id.change_password_button);
        delete_profile_button = findViewById(R.id.delete_profile_button);
        continue_button = findViewById(R.id.continue_button);
    }

    @SuppressLint("SetTextI18n")
    private void fillDetails() {
        if (user != null) {
            display_name_edit.setText(user.getDisplayName());
            password_edit.setText("123456");
        }
    }

    private void initListeners() {
        change_display_name_button.setOnClickListener(v -> {
            String displayName = display_name_edit.getText().toString().trim();
            if (display_name_edit.getText().toString().trim().isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Please insert a valid user name", Toast.LENGTH_SHORT).show();
                return;
            }
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(displayName).build()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DatabaseReference ref = root.child(user.getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            if (userDetails != null) {
                                FamilyProfile familyProfile = userDetails.getFamilyProfile();
                                if (familyProfile != null) {
                                    familyProfile.setUser_name(displayName);
                                    ref.setValue(userDetails);
                                }
                                Toast.makeText(SettingsActivity.this, "User name has successfully updated!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        });
        change_password_button.setOnClickListener(v -> {
            String password = password_edit.getText().toString().trim();
            if (display_name_edit.getText().toString().trim().isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Please insert a valid password", Toast.LENGTH_SHORT).show();
                return;
            }
            user.updatePassword(password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Password has successfully updated!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        delete_profile_button.setOnClickListener(v -> user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                root.child(user.getUid()).removeValue();
                startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
                finishAffinity();
            }
        }));
        continue_button.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, UserDetailsFormActivity.class)));
    }
}
