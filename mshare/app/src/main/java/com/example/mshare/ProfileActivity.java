package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ImageView avatarView;
    private TextView userNameView, userEmailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        avatarView = findViewById(R.id.avatar);
        userNameView = findViewById(R.id.userName);
        userEmailView = findViewById(R.id.userEmail);


        Intent intent = getIntent();
        String userId = intent.getExtras().getString("userId");
        db.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.getString("name");
                String userAvatar = documentSnapshot.getString("avatar");
                String userEmail = documentSnapshot.getString("email");

                userNameView.setText(userName);
                Glide.with(ProfileActivity.this).load(userAvatar).into(avatarView);
                userEmailView.setText(userEmail);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void onLogout(View v){
        updateUserStatus("Offline");
        firebaseAuth.signOut();
        LoginManager.getInstance().logOut();
        Intent intent1 = new Intent(ProfileActivity.this, LoginActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
        |Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
        finish();
    }
    private void updateUserStatus(String status) {
        db.collection("users").document(firebaseAuth.getCurrentUser().getUid()).update("onlineStatus", status);
    }
    public void editFav(View v){
        Intent intent = new Intent(this, EditFavoriteActivity.class);
        startActivity(intent);
    }
}