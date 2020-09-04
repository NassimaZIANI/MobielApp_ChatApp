package com.example.dell.chatapp.Model;

/**
 * Created by Dell on 04/09/2020.
 */

public class Chatlist {

    public String id;

    //constructor
    public Chatlist(String id) {
        this.id = id;
    }

    public Chatlist() {
    }

    //getter & setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
