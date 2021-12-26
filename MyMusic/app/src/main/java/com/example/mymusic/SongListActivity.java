package com.example.mymusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SongListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Song> songArrayList;
    SongAdapter songsAdapter;
    FirebaseFirestore db;
    ProgressDialog progressDialog;

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

        songsAdapter = new SongAdapter(SongListActivity.this, songArrayList);

        recyclerView.setAdapter(songsAdapter);
        EventChangeListener();

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

                        songArrayList.add(dc.getDocument().toObject(Song.class));

                    }

                    songsAdapter.notifyDataSetChanged();
                    if (progressDialog.isShowing()) progressDialog.dismiss();

                }

            }
        });

    }
}