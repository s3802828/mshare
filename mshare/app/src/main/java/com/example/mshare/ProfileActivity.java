package com.example.mshare;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mshare.models.Favorite;
import com.example.mshare.models.Genre;
import com.example.mshare.models.Song;
import com.example.mshare.models.Tokens;
import com.example.mshare.utilClasses.ApplicationStatus;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.util.Hex;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.InvalidProtocolBufferException;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ImageView avatarView;
    private TextView userNameView, userEmailView;
    private Favorite favorite;
    private LinearLayout favSongLayout, favArtistLayout, favGenreLayout;
    private EditText nameInput;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        avatarView = findViewById(R.id.avatar);
        userNameView = findViewById(R.id.userName);
        userEmailView = findViewById(R.id.userEmail);
        nameInput = findViewById(R.id.userNameEdit);

        ImageButton editName = findViewById(R.id.editName_btn);
        ImageButton editFav = findViewById(R.id.editFav_btn);
        Button logout = findViewById(R.id.logout);
        Intent intent = getIntent();
        userId = intent.getExtras().getString("userId");
        if(!userId.equals(firebaseAuth.getCurrentUser().getUid())){
            editName.setVisibility(View.GONE);
            editFav.setVisibility(View.GONE);
            logout.setVisibility(View.GONE);
        }
        db.collection("users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = documentSnapshot.getString("name");
                String userAvatar = documentSnapshot.getString("avatar");
                String userEmail = documentSnapshot.getString("email");

                userNameView.setText(userName);
                nameInput.setText(userName);
                Glide.with(ProfileActivity.this).load(userAvatar).into(avatarView);
                userEmailView.setText(userEmail);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
        favArtistLayout = findViewById(R.id.fav_artist_group);
        favSongLayout = findViewById(R.id.fav_song_group);
        favGenreLayout = findViewById(R.id.fav_genre_group);

    }

    @Override
    protected void onResume() {
        super.onResume();
        favArtistLayout.removeAllViews();
        favSongLayout.removeAllViews();
        favGenreLayout.removeAllViews();
        db.collection("favorites").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        favorite = documentSnapshot.toObject(Favorite.class);
                        if(favorite != null){
                            LinearLayout.LayoutParams songLayoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            songLayoutParams.setMargins(0,0, 5, 5);
                            LinearLayout.LayoutParams artistLayoutParams = new LinearLayout.LayoutParams(
                                    0,
                                    200,
                                    1.0f
                            );
                            artistLayoutParams.setMargins(0,0, 2, 5);
                            for (Song song : favorite.getSongs()) {
                                TextView newView = new TextView(ProfileActivity.this);
                                newView.setText(song.getTitle() +" - " + song.getArtist());
                                newView.setPadding(10, 10, 10, 10);
                                newView.setTextColor(Color.BLACK);
                                newView.setTextSize(16);
                                newView.setGravity(Gravity.CENTER);
                                newView.setBackgroundResource(R.drawable.genre_border);
                                newView.setLayoutParams(songLayoutParams);
                                favSongLayout.addView(newView);
                            }
                            for (int i = 0; i < favorite.getArtists().size(); i += 2) {
                                LinearLayout newRowLayout = new LinearLayout(ProfileActivity.this);
                                newRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                                newRowLayout.setLayoutParams(songLayoutParams);
                                newRowLayout.setWeightSum(2.0f);
                                for (int j = 0; j < 2; j++) {
                                    if(i+j == favorite.getArtists().size()) break;
                                    TextView newView = new TextView(ProfileActivity.this);
                                    newView.setText(favorite.getArtists().get(i+j));
                                    newView.setPadding(10, 10, 10, 10);
                                    newView.setTextColor(Color.BLACK);
                                    newView.setTextSize(16);
                                    newView.setGravity(Gravity.CENTER);
                                    newView.setBackgroundResource(R.drawable.artist_border);
                                    newView.setLayoutParams(artistLayoutParams);
                                    newRowLayout.addView(newView);
                                }
                                favArtistLayout.addView(newRowLayout);
                            }

                            for (int i = 0; i < favorite.getGenres().size(); i += 2) {
                                LinearLayout newRowLayout = new LinearLayout(ProfileActivity.this);
                                newRowLayout.setOrientation(LinearLayout.HORIZONTAL);
                                newRowLayout.setLayoutParams(songLayoutParams);
                                newRowLayout.setWeightSum(2.0f);
                                for (int j = 0; j < 2; j++) {
                                    if(i+j == favorite.getGenres().size()) break;
                                    TextView newView = new TextView(ProfileActivity.this);
                                    newView.setText(favorite.getGenres().get(i+j).getGenreName());
                                    newView.setTextColor(Color.WHITE);
                                    newView.setTextSize(16);
                                    newView.setGravity(Gravity.CENTER);
                                    newView.setTypeface(null, Typeface.BOLD);
                                    newView.setPadding(10, 10, 10, 10);
                                    newView.setBackgroundResource(R.drawable.genre_border);
                                    //newView.setBackgroundColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    newView.setLayoutParams(artistLayoutParams);
                                    Drawable background = newView.getBackground().mutate();
                                    if (background instanceof ShapeDrawable) {
                                        ((ShapeDrawable)background).getPaint().setColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    } else if (background instanceof GradientDrawable) {
                                        ((GradientDrawable)background).setColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    } else if (background instanceof ColorDrawable) {
                                        ((ColorDrawable)background).setColor(Color.parseColor(favorite.getGenres().get(i+j).getGenreColor()));
                                    }
                                    newRowLayout.addView(newView);
                                }
                                favGenreLayout.addView(newRowLayout);
                            }
                        } else {
                            favorite = new Favorite();
                            favorite.setArtists(new ArrayList<>());
                            favorite.setSongs(new ArrayList<>());
                            favorite.setGenres(new ArrayList<>());
                        }
                    }
                });
    }
    @SuppressLint("SetTextI18n")
    public void onEditName(View v){
        TextView name = findViewById(R.id.userName);
        ImageButton nameEditBtn = findViewById(R.id.editName_btn);
        String nameValue = nameInput.getText().toString();
        if(name.getVisibility() == View.VISIBLE){
            name.setVisibility(View.GONE);
            nameInput.setVisibility(View.VISIBLE);
            nameEditBtn.setImageResource(R.drawable.ic_check);
        } else {
            TextView invalidName = findViewById(R.id.invalidName);
            //Validate name
            if(nameValue.equals("")){
                invalidName.setText("Name is required");
                invalidName.setVisibility(View.VISIBLE);
            } else if (!nameValue.matches("^(?![ ]+$)[a-zA-Z .]*$")){
                invalidName.setText("Name must only contain letters and space");
                invalidName.setVisibility(View.VISIBLE);
            } else invalidName.setVisibility(View.GONE);
            if(invalidName.getVisibility() == View.GONE){
                if(name.getText().toString().equals(nameValue.trim())){
                    name.setVisibility(View.VISIBLE);
                    nameInput.setVisibility(View.GONE);
                    nameEditBtn.setImageResource(R.drawable.ic_edit_profile);
                } else {
                    db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                            .update("name", nameValue.trim())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    name.setText(nameValue.trim());
                                    name.setVisibility(View.VISIBLE);
                                    nameInput.setVisibility(View.GONE);
                                    nameEditBtn.setImageResource(R.drawable.ic_edit_profile);
                                }
                            });
                }
            }
        }
    }

    public void onLogout(View v){
        db.collection("tokens").document(firebaseAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        Tokens tokensList = documentSnapshot.toObject(Tokens.class);
                        ArrayList<String> tokens = tokensList.getNames();
                        tokens.remove(ApplicationStatus.getCurrentToken());
                        tokensList.setNames(tokens);
                        db.collection("tokens")
                                .document(firebaseAuth.getCurrentUser().getUid()).set(tokensList)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(@NonNull Void unused) {
                                        firebaseAuth.signOut();
                                        LoginManager.getInstance().logOut();
                                        Intent intent1 = new Intent(ProfileActivity.this, LoginActivity.class);
                                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                |Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent1);
                                        finish();
                                    }
                                });
                    }
                });
    }
    private void updateUserStatus() {

    }
    public void editFav(View v){
        Intent intent = new Intent(this, EditFavoriteActivity.class);
        intent.putExtra("favorites", favorite);
        startActivity(intent);
    }

    public void showFav(View view) {
        RelativeLayout favLayout = findViewById(R.id.fav_group);
        ImageButton showButton = findViewById(R.id.showFav_btn);
        if(favLayout.getVisibility() == View.GONE){
            favLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }

    public void showArtist(View view) {
        ImageButton showButton = findViewById(R.id.showArtist_btn);
        if(favArtistLayout.getVisibility() == View.GONE){
            favArtistLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favArtistLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }

    public void showSong(View view) {
        ImageButton showButton = findViewById(R.id.showSong_btn);
        if(favSongLayout.getVisibility() == View.GONE){
            favSongLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favSongLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }

    public void showGenre(View view) {
        ImageButton showButton = findViewById(R.id.showGenre_btn);
        if(favGenreLayout.getVisibility() == View.GONE){
            favGenreLayout.setVisibility(View.VISIBLE);
            showButton.setImageResource(R.drawable.ic_up);
        } else {
            favGenreLayout.setVisibility(View.GONE);
            showButton.setImageResource(R.drawable.ic_down);
        }
    }
}