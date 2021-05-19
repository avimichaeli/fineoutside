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
import com.example.fineoutside.activities.UserDetailsFormActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText email_log_in;
    EditText password_log_in;

    ProgressBar progressbar_log_in;

    FirebaseAuth auth;

    Button log_in_verify_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toast.makeText(LoginActivity.this, "Please validate your email address", Toast.LENGTH_SHORT).show();

        email_log_in = findViewById(R.id.Email_sign_up_verify);
        password_log_in = findViewById(R.id.password_sign_up_verify);

        progressbar_log_in = findViewById(R.id.progressBar_log_in_verify);

        auth = FirebaseAuth.getInstance();

        log_in_verify_button = findViewById(R.id.log_in_verify_sign_up_button);

        log_in_verify_button.setOnClickListener(v -> {

            progressbar_log_in.setVisibility(View.VISIBLE);

            String verify_email = email_log_in.getText().toString().trim();
            String verify_password = password_log_in.getText().toString().trim();

            if (TextUtils.isEmpty(verify_email)) {
                email_log_in.setError("email is required");
                return;
            }

            if (TextUtils.isEmpty(verify_password)) {
                password_log_in.setError("password is required");
                return;
            }

            // authentication of the user //

            auth.signInWithEmailAndPassword(verify_email, verify_password).addOnCompleteListener(task -> {
                progressbar_log_in.setVisibility(View.GONE);
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    if (task.isSuccessful() && user.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "logged in ", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, UserDetailsFormActivity.class));
                        finish();
                    } else if (task.getException() != null) {
                        Toast.makeText(LoginActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    } else if (!user.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "Please validate your email address", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}

