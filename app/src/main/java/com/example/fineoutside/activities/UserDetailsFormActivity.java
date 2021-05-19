package com.example.fineoutside.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.example.fineoutside.R;
import com.example.fineoutside.data.UserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class UserDetailsFormActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");
    private final StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

    private ImageView profilePicImage;

    private final ActivityResultLauncher<Void> takePicture = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), result -> {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
            ref.putBytes(data).addOnCompleteListener(task -> user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(ref.getPath())).build()));
            Glide.with(UserDetailsFormActivity.this).load(result).into(profilePicImage);
        }
    });

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            StorageReference ref = storageRef.child(System.currentTimeMillis() + ".jpg");
            ref.putFile(result).addOnCompleteListener(task -> user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(ref.getPath())).build()));
            Glide.with(UserDetailsFormActivity.this).load(result).into(profilePicImage);
        }
    });

    private View businessNameContainer, foodTypeContainer, ageContainer, aboutMeContainer;
    private EditText businessNameEdit, ageEdit, aboutMeEdit;
    private Spinner userTypeSpinner, foodTypeSpinner;
    private SwitchCompat picsSwitch, pushSwitch;
    private Button finishButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_details);
        initUI();
        fillDetails();
        initListeners();
    }

    protected void initUI() {
        profilePicImage = findViewById(R.id.profile_pic_image);
        userTypeSpinner = findViewById(R.id.user_type_spinner);
        businessNameContainer = findViewById(R.id.business_name_container);
        foodTypeContainer = findViewById(R.id.food_type_container);
        businessNameEdit = findViewById(R.id.business_name_edit);
        foodTypeSpinner = findViewById(R.id.food_type_spinner);
        ageContainer = findViewById(R.id.age_container);
        ageEdit = findViewById(R.id.age_edit);
        aboutMeContainer = findViewById(R.id.about_me_container);
        aboutMeEdit = findViewById(R.id.about_me_edit);
        picsSwitch = findViewById(R.id.pics_switch);
        pushSwitch = findViewById(R.id.push_switch);
        finishButton = findViewById(R.id.finish_button);
    }

    private void fillDetails() {
        if (user != null) {
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().toString().isEmpty()) {
                Glide.with(this).load(user.getPhotoUrl()).into(profilePicImage);
            }
            root.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserDetails userDetails = snapshot.getValue(UserDetails.class);
                    if (userDetails != null) {
                        picsSwitch.setChecked(userDetails.isEnablePics());
                        pushSwitch.setChecked(userDetails.isEnablePush());
                        userTypeSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.user_types)).indexOf(userDetails.getUserType()));
                        if (userDetails.getBusinessName() != null) {
                            businessNameEdit.setText(userDetails.getBusinessName());
                        }
                        if (userDetails.getFoodType() != null) {
                            foodTypeSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.food_types)).indexOf(userDetails.getFoodType()));
                        }
                        if (userDetails.getAge() != null) {
                            ageEdit.setText(userDetails.getAge());
                        }
                        if (userDetails.getAbout_me() != null) {
                            aboutMeEdit.setText(userDetails.getAbout_me());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void initListeners() {
        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    businessNameContainer.setVisibility(View.VISIBLE);
                    foodTypeContainer.setVisibility(View.VISIBLE);
                    ageContainer.setVisibility(View.GONE);
                    aboutMeContainer.setVisibility(View.GONE);
                } else if (position == 2) {
                    businessNameContainer.setVisibility(View.GONE);
                    foodTypeContainer.setVisibility(View.GONE);
                    ageContainer.setVisibility(View.VISIBLE);
                    aboutMeContainer.setVisibility(View.VISIBLE);
                } else {
                    businessNameContainer.setVisibility(View.GONE);
                    foodTypeContainer.setVisibility(View.GONE);
                    ageContainer.setVisibility(View.GONE);
                    aboutMeContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        profilePicImage.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle(R.string.add_photo)
                .setMessage(R.string.select_option)
                .setPositiveButton(R.string.camera, (dialog, which) -> takePicture.launch(null))
                .setNegativeButton(R.string.photo_library, (dialog, which) -> pickImage.launch("image/*"))
                .setNeutralButton(R.string.cancel, null)
                .create()
                .show());

        finishButton.setOnClickListener(v -> {
            if (user != null) {
                if (userTypeSpinner.getSelectedItemPosition() == 1 && businessNameEdit.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "You have to insert business name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userTypeSpinner.getSelectedItemPosition() == 2 && ageEdit.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "You have to insert age", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (userTypeSpinner.getSelectedItemPosition() == 2 && aboutMeEdit.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "You have to insert about me details", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    String token = task.getResult();
                    root.child(user.getUid())
                        .setValue(new UserDetails(
                            user.getUid(),
                            user.getDisplayName(),
                            userTypeSpinner.getSelectedItem().toString(),
                            picsSwitch.isChecked(),
                            pushSwitch.isChecked(),
                            token,
                            userTypeSpinner.getSelectedItemPosition() == 1 ? businessNameEdit.getText().toString() : null,
                            userTypeSpinner.getSelectedItemPosition() == 1 ? foodTypeSpinner.getSelectedItem().toString() : null,
                            userTypeSpinner.getSelectedItemPosition() == 2 ? ageEdit.getText().toString() : null,
                            userTypeSpinner.getSelectedItemPosition() == 2 ? aboutMeEdit.getText().toString() : null,
                            null,
                            user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString(),
                            System.currentTimeMillis()));
                    moveToTheNextScreen();
                });
            }
        });
    }

    private void moveToTheNextScreen() {
        Intent intent = new Intent(UserDetailsFormActivity.this,
            userTypeSpinner.getSelectedItemPosition() == 1 ? BusinessProfileActivity.class :
                userTypeSpinner.getSelectedItemPosition() == 2 ? BabysitterProfileActivity.class : MainMenuActivity.class);
        startActivity(intent);
        finishAffinity();
    }
}
