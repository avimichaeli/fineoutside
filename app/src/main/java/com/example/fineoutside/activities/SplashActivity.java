package com.example.fineoutside.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fineoutside.R;
import com.example.fineoutside.authentication.LoginActivity;
import com.example.fineoutside.data.UserDetails;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

@SuppressLint("RestrictedApi")
public class SplashActivity extends AppCompatActivity {

    private final static int SPLASH_TIME_OUT = 3000;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference root = database.getReference().child("fine outside users");
    // Create and launch sign-in intent
    final ActivityResultLauncher<Intent> launchSignInIntent = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // Setting user profile pic
                IdpResponse response = IdpResponse.fromResultIntent(result.getData());
                if (response != null) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(response.getUser().getPhotoUri()).build());
                    }
                }

                // Successfully signed in
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    handleUserEmailVerification(user);
                }
            } else {
                // Sign in failed
                Toast.makeText(SplashActivity.this, "There was a problem to sign in", Toast.LENGTH_SHORT).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            FirebaseUser user = auth.getCurrentUser();
            // Determine the destination intent by current user belonging
            if (user == null) {
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build());
                launchSignInIntent.launch(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setTheme(R.style.Theme_Fineoutside)
                    .setLogo(R.drawable.fine_outside_logo)
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(false)
                    .build());
            } else {
                handleUserEmailVerification(user);
            }
        }, SPLASH_TIME_OUT);
    }

    private void handleUserEmailVerification(FirebaseUser user) {
        if (user.isEmailVerified() || user.getProviderId().equalsIgnoreCase("email")) {
            root.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(user.getUid())) {
                        UserDetails userDetails = snapshot.child(user.getUid()).getValue(UserDetails.class);
                        if (userDetails != null) {
                            if (userDetails.getUserType().equalsIgnoreCase("Regular")) {
                                moveToMainMenu();
                            } else if (userDetails.getUserType().equalsIgnoreCase("Business Owner")) {
                                moveToBusinessProfile();
                            } else {
                                moveToBabysitterProfile();
                            }
                        }
                    } else {
                        moveToFormDetails();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            boolean passwordProvider = false;
            List<? extends UserInfo> list = user.getProviderData();
            if (!list.isEmpty()) {
                for (UserInfo userInfo : list) {
                    if (userInfo.getProviderId().equalsIgnoreCase("password")) {
                        passwordProvider = true;
                    }
                }
            }
            if (passwordProvider) {
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                });
            } else {
                moveToFormDetails();
            }
        }
    }

    private void moveToMainMenu() {
        Intent mainMenuIntent = new Intent(SplashActivity.this, MainMenuActivity.class);
        startActivity(mainMenuIntent);
        finish();
    }

    private void moveToFormDetails() {
        Intent formDetailsIntent = new Intent(SplashActivity.this, UserDetailsFormActivity.class);
        startActivity(formDetailsIntent);
        finish();
    }

    private void moveToBusinessProfile() {
        Intent businessProfileIntent = new Intent(SplashActivity.this, BusinessProfileActivity.class);
        startActivity(businessProfileIntent);
        finish();
    }

    private void moveToBabysitterProfile() {
        Intent babysitterProfileIntent = new Intent(SplashActivity.this, BabysitterProfileActivity.class);
        startActivity(babysitterProfileIntent);
        finish();
    }
}