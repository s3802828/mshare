package com.example.mshare.models;

import java.io.Serializable;

public class Genre implements Serializable {
    private String genreName;
    private String genreColor;

    public Genre(String genreName, String genreColor) {
        this.genreName = genreName;
        this.genreColor = genreColor;
    }

    public Genre() {
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getGenreColor() {
        return genreColor;
    }

    public void setGenreColor(String genreColor) {
        this.genreColor = genreColor;
    }
}
