package com.example.dell.chatapp.Model;

/**
 * Created by Dell on 19/08/2020.
 */

public class User {

    private String id;
    private String username;
    private String imgURL;

    // constructor
    public User(String id, String username, String imgURL) {
        this.id = id;
        this.username = username;
        this.imgURL = imgURL;
    }

    public User() {

    }

    // getters & setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }
}
