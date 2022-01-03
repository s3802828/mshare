package com.example.mymusic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SongListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Song> songArrayList;
    ArrayList<Song> songArrayListCopy;
    SongAdapter songsAdapter;
    FirebaseFirestore db;
    ProgressDialog progressDialog;
    SearchView searchSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Collecting songs...");
        progressDialog.show();

        recyclerView = findViewById(R.id.songRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        songArrayList = new ArrayList<Song>();
        songArrayListCopy = new ArrayList<>();

        songsAdapter = new SongAdapter(SongListActivity.this, songArrayList, songArrayListCopy);

//        songsAdapter.setSongArrayListFull();
        recyclerView.setAdapter(songsAdapter);
        EventChangeListener();

        searchSong = findViewById(R.id.searchViewSong);

        searchSong.setQueryHint("Enter song name...");
        searchSong.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                songsAdapter.getFilter().filter(newText);
                return false;
            }
        });

    }

    private void EventChangeListener() {

        db.collection("songs").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null) {

                    if (progressDialog.isShowing()) progressDialog.dismiss();

                    Log.d("Firestore error", error.getMessage());
                    return;
                }

                for (DocumentChange dc: value.getDocumentChanges()) {

                    if (dc.getType() == DocumentChange.Type.ADDED) {

                        String songId = dc.getDocument().getId();
                        String songTitle = dc.getDocument().get("title")+"";
                        String songArtist = dc.getDocument().get("artist")+"";
                        String songCover = dc.getDocument().get("cover")+"";
                        String songUrl = dc.getDocument().get("url")+"";


                        songArrayList.add(new Song(songId, songTitle, songUrl, songCover, songArtist));
                        songArrayListCopy.add(new Song(songId, songTitle, songUrl, songCover, songArtist));
                    }

                    songsAdapter.notifyDataSetChanged();
                    if (progressDialog.isShowing()) progressDialog.dismiss();

                }

            }
        });

    }

    public void playSong(View view) {

        if (songsAdapter.getSongList().equals("")) {
            Toast.makeText(SongListActivity.this, "Song list is empty. Select the song first!", Toast.LENGTH_SHORT).show();
        } else {
            Map<String, String> data = new HashMap<String, String>();

            data.put("list", songsAdapter.getSongList());
            data.put("current_song","");
            data.put("host","");
            data.put("guest","");
            data.put("current_duration","");
            data.put("response","");

            Toast.makeText(SongListActivity.this, "Setting up media player...", Toast.LENGTH_SHORT).show();
            db.collection("room").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Intent intent = new Intent(SongListActivity.this, MainActivity.class);
                    intent.putExtra("room_id", documentReference.getId());
                    startActivity(intent);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SongListActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}