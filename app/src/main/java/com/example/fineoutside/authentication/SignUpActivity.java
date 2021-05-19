package com.example.fineoutside.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fineoutside.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    EditText userName;
    EditText email;
    EditText password;

    Button sign_up_verify_screen;

    FirebaseAuth auth;

    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userName = findViewById(R.id.user_name_log_in_verify);
        email = findViewById(R.id.Email_log_in_verify);
        password = findViewById(R.id.password_log_in_verify);

        sign_up_verify_screen = findViewById(R.id.sign_up_verify_sign_up_button);

        auth = FirebaseAuth.getInstance();

        progressbar = findViewById(R.id.progressBar);

        sign_up_verify_screen.setOnClickListener(v -> {

            progressbar.setVisibility(View.VISIBLE);

            // to check that the strings are empty from previous sign-ups processes
            String verify_user_name = userName.getText().toString().trim();
            String verify_email = email.getText().toString().trim();
            String verify_password = password.getText().toString().trim();

            if (TextUtils.isEmpty(verify_user_name)) {
                userName.setError("user name is required");
                return;
            }

            if (TextUtils.isEmpty(verify_email)) {
                email.setError("email is required");
                return;
            }

            if (TextUtils.isEmpty(verify_password)) {
                password.setError("password is required");
                return;
            }

            if (verify_password.length() < 6) {
                password.setError("password must be at least 6 characters");
                return;
            }

            // register the user in the firebase data //
            auth.createUserWithEmailAndPassword(verify_email, verify_password).addOnCompleteListener(task -> {
                progressbar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "user was created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                } else if (task.getException() != null) {
                    Toast.makeText(SignUpActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}

