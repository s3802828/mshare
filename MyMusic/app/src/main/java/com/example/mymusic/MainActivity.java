package com.example.mymusic;

import androidx.annotation.NonNull;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private SeekBar seekBarDuration;
    private ImageButton playBtn;
    private int position = 0;
    private ArrayList<String> songListId;
    private TextView songTitle;
    private ImageView songCover;
    private TextView txtDuration;
    private TextView txtTotalDuration;
    private int totalDuration;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songListId = new ArrayList<>();
        songs = new ArrayList<Song>();

        songCover = findViewById(R.id.imageCoverMedia);
        playBtn = (ImageButton) findViewById(R.id.imageButton);
        seekBarDuration = (SeekBar) findViewById(R.id.seekBarDuration);
        txtDuration = (TextView) findViewById(R.id.textViewDuration);
        txtTotalDuration = (TextView) findViewById(R.id.textViewTotalDuration);
        songTitle = (TextView) findViewById(R.id.textSong);


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Collecting songs...");
        progressDialog.show();



    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, connection, this.BIND_AUTO_CREATE);
        startService(intent);

    }

    private void startMusicPlayer() {
        DocumentReference docRef = db.collection("room").document("MrsOi6VtwVM3ZOrkDQPj");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {

                    String[] songId = documentSnapshot.getString("list").toString().split(",");
                    System.out.println();

                    songListId.addAll(Arrays.asList(songId));
                    System.out.println(songs.toString());

                    db.collection("songs").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot value: queryDocumentSnapshots) {
                                if (songListId.contains(value.getId())) {
                                    songs.add(value.toObject(Song.class));
                                }
                            }

                            for (Song song: songs) {
                                System.out.println(song.getTitle());
                            }

                            if (serviceConnected) {
                                System.out.println("connected");
                                if (progressDialog.isShowing()) progressDialog.dismiss();
                                System.out.println(songs.toString());
                                musicService.setSongs(songs);

                                try {
                                    totalDuration =  musicService.playSong(0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                                seekBarDuration.setMax(totalDuration);
                                txtTotalDuration.setText(timeCoverter(totalDuration));
                                playBtn.setImageResource(R.drawable.pause);

                                startSeekBar();
                            } else {
                                System.out.println("Service fail");
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("fail");
                        }
                    });


                } else {
                    Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
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
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        new Thread(new Runnable() {
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
        }).start();
    }

//    private void initializeMusicPlayer(int pos) throws IOException {
//
//        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
//            mediaPlayer.reset();
//        }
//
//        System.out.println(pos);
//        String songId = songListId.get(pos);
//        System.out.println(songId);
//
//        DocumentReference songRef = db.collection("songs").document(songId);
//        songRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                if(documentSnapshot.exists()) {
//                    try {
//                        Glide.with(MainActivity.this).load(documentSnapshot.getString("cover")).into(songCover);
//                        mediaPlayer.setDataSource(documentSnapshot.getString("url"));
//                        mediaPlayer.prepare();
//
//                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                            @SuppressLint("SetTextI18n")
//                            @Override
//                            public void onPrepared(MediaPlayer mp) {
//
//                                seekBarDuration.setMax(mediaPlayer.getDuration());
//                                txtTotalDuration.setText(timeCoverter(mediaPlayer.getDuration()));
//                                playBtn.setImageResource(R.drawable.pause);
//                                mediaPlayer.start();
//                            }
//                        });
//
//                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                            @Override
//                            public void onCompletion(MediaPlayer mp) {
////                                playBtn.setImageResource(R.drawable.play);
//                                mp.reset();
//                                if (position < songListId.size()-1) {
//                                    position++;
//                                } else {
//                                    position = 0;
//                                }
//
//                                try {
//                                    initializeMusicPlayer(position);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//
//                        //Code for seekbar
//                        seekBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                            @Override
//                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//                                if(fromUser) {
//                                    seekBarDuration.setProgress(progress);
//
//                                    mediaPlayer.seekTo(progress);
//
//                                    txtDuration.setText(timeCoverter(mediaPlayer.getCurrentPosition()));
//                                }
//                            }
//
//                            @Override
//                            public void onStartTrackingTouch(SeekBar seekBar) {
//
//                            }
//
//                            @Override
//                            public void onStopTrackingTouch(SeekBar seekBar) {
//
//                            }
//                        });
//
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                while(mediaPlayer != null) {
//                                    try {
//                                        if (mediaPlayer.isPlaying()) {
//                                            Message message = new Message();
//                                            message.what = mediaPlayer.getCurrentPosition();
//                                            handler.sendMessage(message);
//                                            Thread.sleep(1000);
//
//                                        }
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//                        }).start();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    songTitle.setText(documentSnapshot.getString("title"));
//                } else {
//                    Toast.makeText(MainActivity.this, "No data", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//
//        }).addOnFailureListener(new OnFailureListener() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                songTitle.setText("Fetch failed");
//            }
//        });
//
//
//    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message message) {
            seekBarDuration.setProgress(message.what);
            txtDuration.setText(timeCoverter(message.what)+"");

        }
    };

//    private void fetchSongs(String songId) {
//
//
//
//            db.collection("songs").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                @Override
//                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                    for (QueryDocumentSnapshot value: queryDocumentSnapshots) {
//                        if (songListId.contains(value.getId())) {
//                            songs.add(value.toObject(Song.class));
//                        }
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @SuppressLint("SetTextI18n")
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    System.out.println("fail");
//                }
//            });
//
//
//    }

    public void manageAudio(View view) {

        if (musicService.getPlayer() != null && musicService.getPlayer().isPlaying()) {
            musicService.pause();

            playBtn.setImageResource(R.drawable.play);
        } else {
            musicService.resume();

            playBtn.setImageResource(R.drawable.pause);
        }
    }

    public void previousSong(View view) throws IOException {

//        mediaPlayer.reset();
//        if (position == 0) {
//            position = songListId.size() - 1;
//        } else {
//            position--;
//        }
//
//        initializeMusicPlayer(position);

        int duration = musicService.playPrev();

        seekBarDuration.setMax(duration);
        txtTotalDuration.setText(timeCoverter(duration));
        playBtn.setImageResource(R.drawable.pause);

    }

    public void nextSong(View view) throws IOException {

//        mediaPlayer.reset();
//        if (position < songListId.size()-1) {
//            position++;
//        } else {
//            position = 0;
//        }
//
//        initializeMusicPlayer(position);

        int duration = musicService.playNext();

        seekBarDuration.setMax(duration);
        txtTotalDuration.setText(timeCoverter(duration));
        playBtn.setImageResource(R.drawable.pause);

    }

    public void toSpotify(View view) {



    }

}