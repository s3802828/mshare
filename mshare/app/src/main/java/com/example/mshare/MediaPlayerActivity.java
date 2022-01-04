package com.example.mshare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mshare.models.Song;
import com.example.mshare.models.User;
import com.example.mshare.services.MusicService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MediaPlayerActivity extends AppCompatActivity {

    private SeekBar seekBarDuration;
    private ImageButton playBtn;
    private int position = 0;
    private ArrayList<String> songListId;
    private TextView songTitle;
    private TextView songArtist;
    private ImageView songCover;
    private TextView txtDuration;
    private TextView txtTotalDuration;
    private int totalDuration;
    private boolean isNext, isPrev;
    private String roomId;
    private String currentSongId;
    private int currentPosition = 0;
    private Button shareButton, endShareButton;
    private Thread thread;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setSongs(songs);
            serviceConnected = true;
            startMusicPlayer();

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceConnected = false;
        }
    };

    private MusicService musicService;
    private boolean serviceConnected = false;
    private ProgressDialog progressDialog;

    ArrayList<Song> songs;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private boolean isSharingMode = false;
    private ListenerRegistration listener1, listener2, listener3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        songListId = new ArrayList<>();
        songs = new ArrayList<Song>();

        songCover = findViewById(R.id.imageCoverMedia);
        playBtn = (ImageButton) findViewById(R.id.imageButton);
        seekBarDuration = (SeekBar) findViewById(R.id.seekBarDuration);
        txtDuration = (TextView) findViewById(R.id.textViewDuration);
        txtTotalDuration = (TextView) findViewById(R.id.textViewTotalDuration);
        songTitle = (TextView) findViewById(R.id.textSong);
        songArtist = (TextView) findViewById(R.id.textArtist);
        songCover = (ImageView) findViewById(R.id.imageCoverMedia);
        shareButton = findViewById(R.id.share_btn);
        endShareButton = findViewById(R.id.end_share_btn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Collecting songs...");
        progressDialog.show();

        Intent intent = getIntent();
        roomId = intent.getExtras().get("room_id")+"";
        if(intent.hasExtra("isSharingMode")) {
            isSharingMode = true;
            shareButton.setVisibility(View.GONE);
            endShareButton.setVisibility(View.VISIBLE);
        }

        listener1 = db.collection("rooms")
                .whereEqualTo(FieldPath.documentId(), roomId)
                .addSnapshotListener(eventListener);

        listener2 = db.collection("rooms")
                .document(roomId)
                .collection("pause_status")
                .whereEqualTo(FieldPath.documentId(), roomId)
                .addSnapshotListener(eventListener1);

         listener3 = db.collection("rooms")
                .document(roomId)
                .collection("request_response")
                .whereEqualTo(FieldPath.documentId(), roomId)
                .addSnapshotListener(eventListener2);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    int currentSong = Integer.parseInt(Objects.requireNonNull(documentChange.getDocument().getString("current_song")));
                    String currentDuration = documentChange.getDocument().getString("current_duration");
                    try {
                        musicService.getPlayer().reset();
                        musicService.playSong(currentSong);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert currentDuration != null;
                    musicService.getPlayer().seekTo(Integer.parseInt(currentDuration));
                    seekBarDuration.setMax(musicService.getTotalDuration());
                    txtTotalDuration.setText(timeCoverter(musicService.getTotalDuration()));
                    songTitle.setText(musicService.getSongTitle());
                    songArtist.setText(musicService.getSongArtist());
                    Glide.with(MediaPlayerActivity.this).load(musicService.getSongCover()).into(songCover);
                }
            }
        }
    };

    private final EventListener<QuerySnapshot> eventListener1 = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    boolean isPause = documentChange.getDocument().getBoolean("isPause");
                    if (isPause) {
                        musicService.pause();
                        playBtn.setImageResource(R.drawable.play);
                    } else {
                        musicService.resume();
                        playBtn.setImageResource(R.drawable.pause);
                    }
                }
            }
        }
    };

    private final EventListener<QuerySnapshot> eventListener2 = (value, error) -> {
        if (error != null) {
            return;
        }
        String res = null;
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    res = documentChange.getDocument().getString("response");
                    assert res != null;
                    if(res.equals("")){
                        isSharingMode = false;
                        shareButton.setVisibility(View.VISIBLE);
                        endShareButton.setVisibility(View.GONE);
                        String guest_id = documentChange.getDocument().getString("guest");
                        if(firebaseAuth.getCurrentUser().getUid().equals(guest_id)) {
                            if(musicService.getPlayer() != null){
                                musicService.getPlayer().reset();
                            }
                            finish();
                        }
                    }
                }
            }
        }
    };

    public void onShare(View v){
        Intent intent = new Intent(MediaPlayerActivity.this, UserListActivity.class);
        intent.putExtra("room_id", roomId);
        startActivityForResult(intent, 100);
    }

    public void onEndShare(View v){
        db.collection("rooms").document(roomId)
                .collection("request_response")
                .document(roomId)
                .update("response", "")
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MediaPlayerActivity.this, MusicService.class);
        bindService(intent, connection, this.BIND_AUTO_CREATE);
        startService(intent);

    }

    private void startMusicPlayer() {
        DocumentReference docRef = db.collection("rooms").document(roomId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {

                    String[] songId = documentSnapshot.getString("list").toString().split(",");
                    System.out.println();

                    songListId.addAll(Arrays.asList(songId));

                    db.collection("songs").whereIn(FieldPath.documentId(), songListId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot value: queryDocumentSnapshots) {
                                String songId = value.getId();
                                String songTitle = value.get("title")+"";
                                String songArtist = value.get("artist")+"";
                                String songCover = value.get("cover")+"";
                                String songUrl = value.get("url")+"";

                                songs.add(new Song(songId, songTitle, songUrl, songCover, songArtist));
                            }

                            if (serviceConnected) {
                                System.out.println("connected");
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                System.out.println(songs.toString());
                                musicService.setSongs(songs);
                                if(isSharingMode){
                                    db.collection("rooms").document(roomId)
                                            .collection("request_response")
                                            .document(roomId)
                                            .update("response", "accept", "guest", firebaseAuth.getCurrentUser().getUid())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(@NonNull Void unused) {

                                        }
                                    });
                                } else {
                                    try {
                                        musicService.playSong(currentPosition);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    updateCurrentSong();
                                }

                                seekBarDuration.setMax(musicService.getTotalDuration());
                                txtTotalDuration.setText(timeCoverter(musicService.getTotalDuration()));
                                playBtn.setImageResource(R.drawable.pause);
                                songTitle.setText(musicService.getSongTitle());
                                songArtist.setText(musicService.getSongArtist());

                                Glide.with(MediaPlayerActivity.this).load(musicService.getSongCover()).into(songCover);
                                startSeekBar();
                            } else {
                                System.out.println("Service fail");
                            }
                        }
                    });


                } else {
                    Toast.makeText(MediaPlayerActivity.this, "No data", Toast.LENGTH_SHORT).show();
                }
            }


        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Exception e) {
                songTitle.setText("Fetch failed");
            }
        });
    }

    private String timeCoverter(long milliseconds) {
        String timer = "";
        String secondsString = "";

        int hours = (int)((milliseconds / (1000 * 60 * 60)) % 60);
        int minutes = (int)(milliseconds / (1000 * 60) % 60);
        int seconds = (int) ((milliseconds / 1000) % 60);

        if (hours > 0) {
            timer = hours+ " :";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        timer = timer + minutes + ":" + secondsString;

        return timer;
    }

    private void startSeekBar() {
        //Code for seekbar
        seekBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(fromUser) {
                    seekBarDuration.setProgress(progress);

                    musicService.getPlayer().seekTo(progress);

                    txtDuration.setText(timeCoverter(musicService.getCurrentPosition()));
                    if(isSharingMode) {
                        updateCurrentDuration(musicService.getCurrentPosition());
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

       thread =  new Thread(new Runnable() {
            @Override
            public void run() {
                while(musicService.getPlayer() != null) {
                    try {
                        if (musicService.getPlayer().isPlaying()) {
                            Message message = new Message();
                            message.what = musicService.getPlayer().getCurrentPosition();
                            handler.sendMessage(message);
                            Thread.sleep(1000);

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
       thread.start();
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message message) {
            seekBarDuration.setProgress(message.what);
            txtDuration.setText(timeCoverter(message.what)+"");
            if (musicService.isSongChanged()) {
                txtTotalDuration.setText(timeCoverter(musicService.getTotalDuration()));
                songTitle.setText(musicService.getSongTitle());
                songArtist.setText(musicService.getSongArtist());
                Glide.with(MediaPlayerActivity.this).load(musicService.getSongCover()).into(songCover);
                musicService.setSongChanged();
            }

        }
    };


    public void manageAudio(View view) {

        if (musicService.getPlayer() != null && musicService.getPlayer().isPlaying()) {
            musicService.pause();
            playBtn.setImageResource(R.drawable.play);
            if(isSharingMode) updatePauseResume(true);
        } else {
            musicService.resume();
            playBtn.setImageResource(R.drawable.pause);
            if(isSharingMode) updatePauseResume(false);
        }
    }

    public void previousSong(View view) throws IOException {

        isPrev = musicService.playPrev();

        seekBarDuration.setMax(musicService.getTotalDuration());
        txtTotalDuration.setText(timeCoverter(musicService.getTotalDuration()));
        songTitle.setText(musicService.getSongTitle());
        songArtist.setText(musicService.getSongArtist());

        Glide.with(MediaPlayerActivity.this).load(musicService.getSongCover()).into(songCover);

        updateCurrentSong();
        playBtn.setImageResource(R.drawable.pause);
        isPrev = false;
    }

    public void nextSong(View view) throws IOException {

        isNext = musicService.playNext();

        seekBarDuration.setMax(musicService.getTotalDuration());
        txtTotalDuration.setText(timeCoverter(musicService.getTotalDuration()));
        songTitle.setText(musicService.getSongTitle());
        songArtist.setText(musicService.getSongArtist());

        Glide.with(MediaPlayerActivity.this).load(musicService.getSongCover()).into(songCover);

        updateCurrentSong();
        playBtn.setImageResource(R.drawable.pause);
        isNext = false;

    }

    public void updateCurrentSong() {
        db.collection("rooms").document(roomId)
                .update("current_song",String.valueOf(musicService.getCurrentSongPosition()), "current_duration", String.valueOf(musicService.getCurrentPosition()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MediaPlayerActivity.this,"Current Song: "+ musicService.getSongTitle(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MediaPlayerActivity.this, "Failed to update current song", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateCurrentDuration(int duration){
        db.collection("rooms").document(roomId)
                .update("current_duration", String.valueOf(duration))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void updatePauseResume(boolean isPause){
        db.collection("rooms").document(roomId)
                .collection("pause_status")
                .document(roomId)
                .update("isPause", isPause).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void unused) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listener1.remove();
        listener2.remove();
        listener3.remove();
        thread.interrupt();
        handler.removeCallbacks(thread);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == 100){
                isSharingMode = true;
                updateCurrentDuration(musicService.getCurrentPosition());
                shareButton.setVisibility(View.GONE);
                endShareButton.setVisibility(View.VISIBLE);
            }
        }
    }
}