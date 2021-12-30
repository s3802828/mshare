package com.example.mshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MediaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        Button button = findViewById(R.id.gotoConversationActivity);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MediaActivity.this, ChatActivity.class);
            startActivity(intent);
            finish();
        });
    }
}