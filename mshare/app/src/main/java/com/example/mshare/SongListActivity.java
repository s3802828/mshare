package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

public class SongListActivity extends AppCompatActivity {
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        Button button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SongListActivity.this, UserListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.conversationList:
                Intent intent = new Intent(SongListActivity.this, ConversationActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                firebaseAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent1 = new Intent(SongListActivity.this, LoginActivity.class);
                setResult(200, intent1);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}