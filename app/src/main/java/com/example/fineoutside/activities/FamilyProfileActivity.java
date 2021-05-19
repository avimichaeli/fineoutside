package com.example.fineoutside.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fineoutside.R;
import com.example.fineoutside.data.ChildNameAge;
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

import java.util.ArrayList;
import java.util.List;

public class FamilyProfileActivity extends LocationActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseUser user = auth.getCurrentUser();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");
    private EditText user_name_edit, age_edit, mate_name_edit, mate_age_edit;
    private TextView mate_name_text, mate_age_text, num_of_children_text;
    private Spinner family_status_spinner, num_of_children_spinner;
    private LinearLayout children_container;
    private Button create_profile_button;
    private List<ChildNameAge> childNameAgeList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_profile);
        initUI();
        fillDetails();
        initListeners();
    }

    protected void initUI() {
        user_name_edit = findViewById(R.id.user_name_edit);
        age_edit = findViewById(R.id.age_edit);
        mate_name_edit = findViewById(R.id.mate_name_edit);
        mate_age_edit = findViewById(R.id.mate_age_edit);
        mate_name_text = findViewById(R.id.mate_name_text);
        mate_age_text = findViewById(R.id.mate_age_text);
        num_of_children_text = findViewById(R.id.num_of_children_text);
        family_status_spinner = findViewById(R.id.family_status_spinner);
        num_of_children_spinner = findViewById(R.id.num_of_children_spinner);
        children_container = findViewById(R.id.children_container);
        create_profile_button = findViewById(R.id.create_profile_button);
    }

    private void fillDetails() {
        FamilyProfile familyProfile = getIntent().getParcelableExtra("FamilyProfile");
        if (familyProfile == null) {
            if (user != null) {
                user_name_edit.setText(user.getDisplayName());
            }
        } else {
            user_name_edit.setText(familyProfile.getUser_name());
            age_edit.setText(String.valueOf(familyProfile.getAge()));
            int familyStatus = familyProfile.getFamily_status();
            family_status_spinner.setSelection(familyStatus);
            if (familyStatus != 0) {
                mate_name_edit.setText(familyProfile.getMate_name());
                mate_age_edit.setText(String.valueOf(familyProfile.getMate_age()));
                childNameAgeList = familyProfile.getChildNameAgeList();
                num_of_children_spinner.setSelection(childNameAgeList == null ? 0 : childNameAgeList.size());
            }
        }
    }

    private void initListeners() {
        family_status_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int visibility = position == 0 ? View.GONE : View.VISIBLE;
                mate_name_text.setVisibility(visibility);
                mate_name_edit.setVisibility(visibility);
                mate_age_text.setVisibility(visibility);
                mate_age_edit.setVisibility(visibility);
                num_of_children_text.setVisibility(visibility);
                num_of_children_spinner.setVisibility(visibility);
                children_container.setVisibility(visibility);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        num_of_children_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                children_container.removeAllViews();
                for (int i = 1; i <= position; i++) {
                    View childAgeView = View.inflate(parent.getContext(), R.layout.view_child_name_age, null);
                    childAgeView.setTag(i);
                    if (childNameAgeList != null && !childNameAgeList.isEmpty()) {
                        if (i - 1 < childNameAgeList.size()) {
                            ((EditText) childAgeView.findViewById(R.id.child_name_edit)).setText(childNameAgeList.get(i - 1).getChild_name());
                            ((EditText) childAgeView.findViewById(R.id.child_age_edit)).setText(String.valueOf(childNameAgeList.get(i - 1).getChild_age()));
                        }
                    }
                    children_container.addView(childAgeView);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        create_profile_button.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("USER_LOCATION", MODE_PRIVATE);
            double latitude = Double.parseDouble(preferences.getString("Latitude", "0.0"));
            double longitude = Double.parseDouble(preferences.getString("Longitude", "0.0"));
            String userName = user_name_edit.getText().toString();
            if (userName.trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid user name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (age_edit.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
                return;
            }
            int age = Integer.parseInt(age_edit.getText().toString());
            int familyStatus = family_status_spinner.getSelectedItemPosition();
            if (familyStatus != 0 && mate_name_edit.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid mate name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (familyStatus != 0 && mate_age_edit.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter a valid mate age", Toast.LENGTH_SHORT).show();
                return;
            }
            String mateName = mate_name_edit.getText().toString();
            int mateAge = familyStatus == 0 ? 0 : Integer.parseInt(mate_age_edit.getText().toString());
            int numOfChildren = num_of_children_spinner.getSelectedItemPosition();
            List<ChildNameAge> childNameAgeList = new ArrayList<>();
            for (int i = 1; i <= numOfChildren; i++) {
                EditText child_name_edit = children_container.findViewWithTag(i).findViewById(R.id.child_name_edit);
                if (child_name_edit.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Please enter a valid child name", Toast.LENGTH_SHORT).show();
                    return;
                }
                EditText child_age_edit = children_container.findViewWithTag(i).findViewById(R.id.child_age_edit);
                if (child_age_edit.getText().toString().trim().isEmpty()) {
                    Toast.makeText(this, "Please enter a valid child age", Toast.LENGTH_SHORT).show();
                    return;
                }
                childNameAgeList.add(new ChildNameAge(child_name_edit.getText().toString(), Integer.parseInt(child_age_edit.getText().toString())));
            }
            FamilyProfile familyProfile = familyStatus == 0 ? new FamilyProfile(userName, age, familyStatus, latitude, longitude, user.getUid(), user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString()) : new FamilyProfile(userName, age, mateAge, familyStatus, numOfChildren, mateName, childNameAgeList, latitude, longitude, user.getUid(), user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString());
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(user_name_edit.getText().toString()).build()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DatabaseReference ref = root.child(user.getUid());
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserDetails userDetails = snapshot.getValue(UserDetails.class);
                            if (userDetails != null) {
                                userDetails.setFamilyProfile(familyProfile);
                                ref.setValue(userDetails);
                                Toast.makeText(FamilyProfileActivity.this, "Family profile has successfully created/updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        });
    }
}
