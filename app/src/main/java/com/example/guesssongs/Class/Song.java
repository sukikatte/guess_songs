package com.example.guesssongs.Class;

public class Song {
    private String title;
    private String artist;
    private String musicUri;
    private String id;  // Add an ID field for the song

    public Song() {
    }

    public Song(String title, String artist, String musicUri, String id) {
        this.title = title;
        this.artist = artist;
        this.musicUri = musicUri;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getMusicUri() {
        return musicUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
