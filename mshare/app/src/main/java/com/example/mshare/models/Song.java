package com.example.mshare.models;

public class Song {

    private String id, title, url, cover, artist;

    public Song(){}

    public Song(String id, String title, String url, String cover, String artist) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.cover = cover;
        this.artist = artist;
    }

    public String getId() {return id;}

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
