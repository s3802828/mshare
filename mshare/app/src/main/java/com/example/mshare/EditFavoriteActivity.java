package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mshare.models.Favorite;
import com.example.mshare.models.Genre;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class EditFavoriteActivity extends AppCompatActivity {
    private LinearLayout favSongEdit, favArtistEdit, favGenreEdit;
    private final LinkedHashMap<Integer, String> addSongRowResult = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, String> addArtistRowResult = new LinkedHashMap<>();
    private ArrayList<Genre> genreChosen;
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_favorite);
        favSongEdit = findViewById(R.id.fav_song_edit);
        favArtistEdit = findViewById(R.id.fav_artist_edit);
        favGenreEdit = findViewById(R.id.fav_genre_edit);
        Intent intent = getIntent();
        genreChosen = ((Favorite) intent.getExtras().get("favorites")).getGenres();
        db.collection("song_fav_genres").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.size() == 0){
                    HashMap<String, String> songGenre = new HashMap<>();
                    songGenre.put("Old School", "#FF0000");
                    songGenre.put("Rock", "#DC143C");
                    songGenre.put("Heavy metal", "#BB0022");
                    songGenre.put("Country", "#A52A2A");
                    songGenre.put("Tango", "#800000");
                    songGenre.put("Gospel", "#A0522D");
                    songGenre.put("Exotica", "#FA8072");
                    songGenre.put("Mariachi", "#E9967A");
                    songGenre.put("Soul/Funk", "#FFA500");
                    songGenre.put("Ska", "#FF8C00");
                    songGenre.put("Folk/Traditional", "#DAA520");
                    songGenre.put("Disco", "#FFA500");
                    songGenre.put("Salsa", "#EEE8AA");
                    songGenre.put("Calypso", "#BDB76B");
                    songGenre.put("Samba", "#9ACD32");
                    songGenre.put("Cumbia", "#7FFFD4");
                    songGenre.put("Contemporary Christian", "#90EE90");
                    songGenre.put("Rumba", "#36A667");
                    songGenre.put("Reggae", "#008000");
                    songGenre.put("Flamenco", "#006400");
                    songGenre.put("Electronic", "#C0C0C0");
                    songGenre.put("New Age/Space music", "#8D9092");
                    songGenre.put("Polka", "#FAEBD7");
                    songGenre.put("Jazz", "#FFC0CB");
                    songGenre.put("Hip hop", "#00008B");
                    songGenre.put("Blues/R&B", "#0000E1");
                    songGenre.put("African popular music", "#00BFFF");
                    songGenre.put("Pop music", "#87CEEB");
                    songGenre.put("A cappella", "#DDBBBB");
                    ArrayList<Genre> genres = new ArrayList<>();
                    songGenre.forEach((key, value) -> {
                        Genre newGenre = new Genre();
                        newGenre.setGenreName(key);
                        newGenre.setGenreColor(value);
                        db.collection("song_fav_genres").add(newGenre).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(@NonNull DocumentReference documentReference) {

                            }
                        });
                        genres.add(newGenre);
                    });
                    generateGenreChoice(genres);
                } else {
                   ArrayList<Genre> genres = new ArrayList<>();
                    for (DocumentSnapshot ds :queryDocumentSnapshots.getDocuments()){
                        Genre newGenre = new Genre(ds.getString("genreName"), ds.getString("genreColor"));
                        genres.add(newGenre);
                    }
                    generateGenreChoice(genres);
                }
            }
        });
    }

    private void generateGenreChoice(ArrayList<Genre> genreChoices){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams checkBoxParam = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
        );

        for (int i = 0; i < genreChoices.size(); i += 3) {
            LinearLayout newRowLayout = new LinearLayout(this);
            newRowLayout.setId(View.generateViewId());
            newRowLayout.setOrientation(LinearLayout.HORIZONTAL);
            newRowLayout.setLayoutParams(layoutParams);
            newRowLayout.setWeightSum(3.0f);
            for (int j = 0; j < 3; j++) {
                if(i+j == genreChoices.size()) break;
                Genre choice = genreChoices.get(i+j);
                String choiceName = genreChoices.get(i+j).getGenreName();
                CheckBox newCheckBox = new CheckBox(this);
                newCheckBox.setId(View.generateViewId());
                newCheckBox.setLayoutParams(checkBoxParam);
                newCheckBox.setText(choiceName);
                if(genreChosen.stream().anyMatch(genre -> genre.getGenreName().equals(choiceName)))
                    newCheckBox.setChecked(true);
                newRowLayout.addView(newCheckBox);
                newCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            genreChosen.add(choice);
                        } else genreChosen.remove(choice);
                    }
                });
            }
            favGenreEdit.addView(newRowLayout);
        }
    }

    @SuppressLint("SetTextI18n")
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
        addSongRowResult.put(newRowLayout.getId(), "");
        TextView validateError = new TextView(this);
        validateError.setText("Song name play by this artist is required");
        validateError.setTextColor(Color.RED);
        validateError.setVisibility(View.GONE);
        favSongEdit.addView(validateError);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSongRowResult.remove(newRowLayout.getId());
                favSongEdit.removeView(newRowLayout);
                favSongEdit.removeView(validateError);
            }
        });

        songNameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateError.setVisibility(View.GONE);
                if(songArtistEdit.getText().toString().equals("")){
                    if(s.toString().equals("")) addSongRowResult.replace(newRowLayout.getId(), "");
                    else addSongRowResult.replace(newRowLayout.getId(), s.toString() + " - Unknown");
                } else if (s.toString().equals("")){
                    validateError.setVisibility(View.VISIBLE);
                    addSongRowResult.replace(newRowLayout.getId(), "");
                }
                else addSongRowResult.replace(newRowLayout.getId(), s.toString() + " - " +
                            songArtistEdit.getText().toString());

            }
        });
        songArtistEdit.addTextChangedListener(new TextWatcher() {
              @Override
              public void beforeTextChanged(CharSequence s, int start, int count, int after) {
              }

              @Override
              public void onTextChanged(CharSequence s, int start, int before, int count) {
              }

              @Override
              public void afterTextChanged(Editable s) {
                  if (songNameEdit.getText().toString().equals("")){
                      if(!s.toString().equals("")){
                          validateError.setVisibility(View.VISIBLE);
                          addSongRowResult.replace(newRowLayout.getId(), "");
                      } else {
                          validateError.setVisibility(View.GONE);
                          addSongRowResult.replace(newRowLayout.getId(), "");
                      }
                  } else if(s.toString().equals("")){
                      addSongRowResult.replace(newRowLayout.getId(), songNameEdit.getText().toString() + " - Unknown");
                  } else addSongRowResult.replace(newRowLayout.getId(),songNameEdit.getText().toString()
                              + " - " + s.toString());
              }
          }
        );
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
        addArtistRowResult.put(newRowLayout.getId(), "");
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addArtistRowResult.remove(newRowLayout.getId());
                favArtistEdit.removeView(newRowLayout);
            }
        });
        songArtistEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                addArtistRowResult.replace(newRowLayout.getId(), s.toString());
            }
        });
    }

    public void goBack(View v){
        finish();
    }

    public void update(View v){
        ArrayList<String> songs = new ArrayList<>(addSongRowResult.values());
        songs.removeIf(song -> song.equals(""));
        ArrayList<String> artists = new ArrayList<>(addArtistRowResult.values());
        artists.removeIf(artist -> artist.equals(""));
        Favorite newFavorite = new Favorite();
        if(songs.size() > 0) newFavorite.setSongs(songs);
        if(artists.size() > 0) newFavorite.setArtists(artists);
        if(genreChosen.size() > 0) newFavorite.setGenres(genreChosen);

        db.collection("favorites")
                .document(firebaseAuth.getCurrentUser().getUid())
                .set(newFavorite)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        Toast.makeText(EditFavoriteActivity.this, "Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }


}