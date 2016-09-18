package com.syntones.model;

import java.util.List;

/**
 * Created by Courtney Love on 9/12/2016.
 */
public class Playlist {

    private Long playlistId;

    private String playlistName;

    private List<Song> songs;

    private User user;

    private String[] songIdList;

    public Playlist() {

    }


    public Playlist(Long playlistId, String playlistName, List<Song> songs, User user, String[] songIdList) {
        super();
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.songs = songs;
        this.user = user;
        this.songIdList = songIdList;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String[] getSongIdList() {
        return songIdList;
    }

    public void setSongIdList(String[] songIdList) {
        this.songIdList = songIdList;
    }

    public Long getPlaylistId() {
        return playlistId;
    }

    public void setPlayListId(Long playListId) {
        this.playlistId = playListId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }


    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }


    public List<Song> getSongs() {
        return songs;
    }


    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }


    @Override
    public String toString() {
        return "Playlist [playlistId=" + playlistId + ", playlistName=" + playlistName + ", songs=" + songs + "]";
    }


}
