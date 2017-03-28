package com.example.elied.hush;


public class SongListElement {
    public SongListElement(int songID) {
        this.songID = songID;
    }

    public int getSongID() {
        return songID;
    }

    public void setSongID(int songID) {
        this.songID = songID;
    }

    private int songID;
}
