package com.example.mshare.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Favorite implements Serializable {
    private ArrayList<String> songs;
    private ArrayList<String> artists;
    private ArrayList<Genre> genres;

    public Favorite() {
    }

    public ArrayList<String> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<String> songs) {
        this.songs = songs;
    }

    public ArrayList<String> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<String> artists) {
        this.artists = artists;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }
}
