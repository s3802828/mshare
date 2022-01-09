package com.example.mshare.models;

import java.io.Serializable;

public class Song implements Serializable {

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

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
