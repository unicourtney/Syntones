package com.syntones.model;

/**
 * Created by Courtney Love on 9/12/2016.
 */
public class Song {

    private long songId;

    private String songTitle;

    private String artistName;

    private String songLyrics;

    private Artist artist;

    public Song() {
    }

    public Song(long songId, String songTitle, String artistName, String songLyrics, Artist artist) {
        this.songId = songId;
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.songLyrics = songLyrics;
        this.artist = artist;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public String getSongLyrics() {
        return songLyrics;
    }

    public void setSongLyrics(String songLyrics) {
        this.songLyrics = songLyrics;
    }
}
