package com.example.mshare.model;

import java.io.Serializable;

public class User implements Serializable {
    public String name, email, id;

    public User(){}
    public User(String name, String email, String id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }
}
