package com.example.mshare.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.example.mshare.models.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MusicBinder binder = new MusicBinder();
    private ArrayList<Song> songs;
    private MediaPlayer player = new MediaPlayer();
    private int currentPosn=0;
    private boolean songChanged = false;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Toast.makeText(this, "Connected to Music Service",
                Toast.LENGTH_SHORT).show();
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializePlayer();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializePlayer() {
        System.out.println("InitializePlayer");
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public int playSong(int position) throws IOException {

        Song song = songs.get(position);

        currentPosn = position;

        player.reset();

        try {
            player.setDataSource(song.getUrl());
            System.out.println(song.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.prepare();

        return player.getDuration();

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        System.out.println("Media prepared");
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        System.out.println("Failed");
        mp.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        try {
            playNext();
            songChanged = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSongChanged() {
        this.songChanged = false;
    }

    public boolean isSongChanged() {
        return this.songChanged;
    }

    public String getSongTitle() {
        Song song = songs.get(currentPosn);

        return song.getTitle();
    }

    public String getSongId() {
        Song song = songs.get(currentPosn);

        return song.getId();
    }

    public String getSongArtist() {
        Song song = songs.get(currentPosn);

        return song.getArtist();
    }

    public String getSongCover() {
        Song song = songs.get(currentPosn);

        return song.getCover();
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public int getTotalDuration() {
        return player.getDuration();
    }

    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getCurrentSongPosition(){
        return currentPosn;
    }

    public void pause() {
        player.pause();
    }

    public void resume() {
        player.start();
    }

    public boolean playNext() throws IOException {
        currentPosn++;
        if (currentPosn == songs.size()) currentPosn = 0;
        playSong(currentPosn);
        return true;
    }

    public boolean playPrev() throws IOException {
        currentPosn--;
        if (currentPosn < 0)
            currentPosn = songs.size() - 1;
        playSong(currentPosn);
        return true;
    }

    public class MusicBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }
}