package com.example.fineoutside.activities;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fineoutside.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView info_title_text = findViewById(R.id.info_title_text);
        TextView info_message_text = findViewById(R.id.info_message_text);

        info_message_text.setMovementMethod(new ScrollingMovementMethod());

        info_title_text.setText(getIntent().getStringExtra("Title"));
        info_message_text.setText(getIntent().getStringExtra("Message"));
    }
}
