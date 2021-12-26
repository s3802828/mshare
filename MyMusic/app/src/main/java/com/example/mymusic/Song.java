package com.example.mymusic;

public class Song {

    private String title, url, cover, artist;

    public Song(){}

    public Song(String title, String url, String cover, String artist) {
        this.title = title;
        this.url = url;
        this.cover = cover;
        this.artist = artist;
    }

    public String getCover() {
        return cover;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }


    public String getUrl() {
        return url;
    }
}
