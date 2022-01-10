package com.example.mshare;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.example.mshare.adapters.SongAdapter;
import com.example.mshare.models.Song;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
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
        Intent intent = getIntent();
        if(intent != null){
            if(intent.hasExtra("isSharingMode")){
                String roomId = intent.getExtras().getString("room_id");
                Intent intent1 = new Intent(this, MediaPlayerActivity.class);
                intent1.putExtra("isSharingMode", true);
                intent1.putExtra("room_id", roomId);
                startActivity(intent1);
            }
        }

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
            Map<String, String> data = new HashMap<>();

            data.put("list", songsAdapter.getSongList());
            data.put("current_song","");
            data.put("host", firebaseAuth.getCurrentUser().getUid());
            data.put("current_duration","");

            HashMap<String, String> res = new HashMap<>();
            res.put("response", "");
            res.put("guest","");

            HashMap<String, Boolean> pause = new HashMap<>();
            pause.put("isPause", false);


            Toast.makeText(SongListActivity.this, "Setting up media player...", Toast.LENGTH_SHORT).show();
            db.collection("rooms").add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference roomDocumentReference) {
                    roomDocumentReference.collection("request_response").document(roomDocumentReference.getId())
                            .set(res)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    Intent intent = new Intent(SongListActivity.this, MediaPlayerActivity.class);
                                    intent.putExtra("room_id", roomDocumentReference.getId());
                                    startActivity(intent);
                                }
                            });
                    roomDocumentReference.collection("pause_status").document(roomDocumentReference.getId())
                            .set(pause)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                }
                            });
                    Intent intent = new Intent(SongListActivity.this, MediaPlayerActivity.class);
                    intent.putExtra("room_id", roomDocumentReference.getId());
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
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_bar_main, menu);
        MenuItem searchViewItem = menu.findItem(R.id.searchView);
        searchSong = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchSong.setQueryHint("Enter Song Name...");
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.conversationList:
                Intent intent = new Intent(SongListActivity.this, ConversationActivity.class);
                startActivity(intent);
                return true;
            case R.id.profilePage:
                Intent intent1 = new Intent(SongListActivity.this, ProfileActivity.class);
                intent1.putExtra("userId", firebaseAuth.getCurrentUser().getUid());
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void toUserList(View v){
        Intent intent = new Intent(SongListActivity.this, UserListActivity.class);
        startActivity(intent);
    }

}
