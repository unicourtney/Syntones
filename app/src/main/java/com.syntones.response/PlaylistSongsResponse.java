package com.syntones.response;

import com.syntones.model.Message;
import com.syntones.model.Song;

import java.util.List;


public class PlaylistSongsResponse {
    private List<Song> songsOnPlaylist;
    private Message message;

    public PlaylistSongsResponse() {
    }


    public List<Song> getSongsOnPlaylist() {
        return songsOnPlaylist;
    }


    public void setSongsOnPlaylist(List<Song> songsOnPlaylist) {
        this.songsOnPlaylist = songsOnPlaylist;
    }


    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }


}
