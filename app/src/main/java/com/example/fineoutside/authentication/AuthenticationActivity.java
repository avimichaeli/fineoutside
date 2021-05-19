package com.example.fineoutside.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fineoutside.R;

public class AuthenticationActivity extends AppCompatActivity {

    Button log_in_button;
    Button sign_up_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        log_in_button = findViewById(R.id.log_in_button);
        sign_up_button = findViewById(R.id.sign_up_button);

        log_in_button.setOnClickListener(v -> open_log_in_verify());
        sign_up_button.setOnClickListener(v -> open_sign_in_verify());
    }

    public void open_log_in_verify() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void open_sign_in_verify() {
        startActivity(new Intent(this, SignUpActivity.class));
    }
}