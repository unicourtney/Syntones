package com.syntones.model;

/**
 * Created by Courtney Love on 9/12/2016.
 */
public class Artist {

    private long artistId;


    private String artistName;

    public Artist() {

    }


    public Artist(String artistName) {
        this.artistName = artistName;
    }

    public long getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

}
