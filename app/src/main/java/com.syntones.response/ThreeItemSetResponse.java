package com.syntones.response;

import com.syntones.model.Message;
import com.syntones.model.Song;
import com.syntones.model.ThreeItemSet;

import java.util.List;


public class ThreeItemSetResponse {
    private Message message;
    private List<Song> songList;

    public ThreeItemSetResponse() {
        super();
    }

    public ThreeItemSetResponse(Message message, List<Song> songList) {
        super();
        this.message = message;
        this.songList = songList;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

}
