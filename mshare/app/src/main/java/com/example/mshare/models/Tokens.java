package com.example.mshare.models;

import java.util.ArrayList;

public class Tokens {
    private String id;
    private ArrayList<String> names;

    public Tokens() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }
}
