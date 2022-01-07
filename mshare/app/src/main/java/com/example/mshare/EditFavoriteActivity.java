package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class EditFavoriteActivity extends AppCompatActivity {
    private LinearLayout favSongEdit, favArtistEdit;
    private final ArrayList<Integer> addSongRowId = new ArrayList<>();
    private final ArrayList<Integer> addArtistRowId = new ArrayList<>();
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_favorite);
        favSongEdit = findViewById(R.id.fav_song_edit);
        favArtistEdit = findViewById(R.id.fav_artist_edit);
        db.collection("song_fave_genres").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size() == 0){

                }
            }
        });
    }

    private void generateGenreChoice(){

    }

    public void onAddFavSongEdit(View v){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams editTextParam = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.2f
        );
        LinearLayout newRowLayout = new LinearLayout(this);
        newRowLayout.setId(View.generateViewId());
        newRowLayout.setOrientation(LinearLayout.HORIZONTAL);
        newRowLayout.setLayoutParams(layoutParams);

        EditText songNameEdit = new EditText(this);
        songNameEdit.setHint(R.string.enter_your_favorite_song_name);
        songNameEdit.setLayoutParams(editTextParam);
        EditText songArtistEdit = new EditText(this);
        songArtistEdit.setHint(R.string.enter_artist_of_song);
        songArtistEdit.setLayoutParams(editTextParam);
        ImageButton dismissButton = new ImageButton(this);
        dismissButton.setImageResource(R.drawable.ic_dismiss);
        dismissButton.setBackgroundColor(Color.TRANSPARENT);
        dismissButton.setLayoutParams(buttonParam);
        newRowLayout.addView(songNameEdit);
        newRowLayout.addView(songArtistEdit);
        newRowLayout.addView(dismissButton);
        favSongEdit.addView(newRowLayout);
        addSongRowId.add(newRowLayout.getId());
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSongRowId.remove((Object) newRowLayout.getId());
                favSongEdit.removeView(newRowLayout);
            }
        });
    }

    public void onAddFavArtistEdit(View view) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams editTextParam = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2.0f
        );
        LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                0.2f
        );
        LinearLayout newRowLayout = new LinearLayout(this);
        newRowLayout.setId(View.generateViewId());
        newRowLayout.setOrientation(LinearLayout.HORIZONTAL);
        newRowLayout.setLayoutParams(layoutParams);

        EditText songArtistEdit = new EditText(this);
        songArtistEdit.setHint(R.string.enter_artist_name);
        songArtistEdit.setLayoutParams(editTextParam);
        ImageButton dismissButton = new ImageButton(this);
        dismissButton.setImageResource(R.drawable.ic_dismiss);
        dismissButton.setBackgroundColor(Color.TRANSPARENT);
        dismissButton.setLayoutParams(buttonParam);
        newRowLayout.addView(songArtistEdit);
        newRowLayout.addView(dismissButton);

        favArtistEdit.addView(newRowLayout);
        addArtistRowId.add(newRowLayout.getId());
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addArtistRowId.remove((Object) newRowLayout.getId());
                favArtistEdit.removeView(newRowLayout);
            }
        });
    }
}