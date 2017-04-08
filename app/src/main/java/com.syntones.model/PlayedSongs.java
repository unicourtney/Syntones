package com.syntones.model;

import java.sql.Timestamp;

/**
 * Created by Courtney Love on 11/17/2016.
 */

public class PlayedSongs {

    private long id, user_id;


    private String session_id, part_of_day;

    private String track_id;

    private Timestamp date;

    public PlayedSongs() {
        super();
    }


    public PlayedSongs(String session_id, String track_id, Timestamp date, long user_id, String part_of_day) {
        super();

        this.session_id = session_id;
        this.track_id = track_id;
        this.date = date;
        this.user_id = user_id;
        this.part_of_day = part_of_day;
    }

    public PlayedSongs(Timestamp date, String track_id, String session_id, long user_id) {
        this.date = date;
        this.track_id = track_id;
        this.session_id = session_id;
        this.user_id = user_id;
    }

    public PlayedSongs(String session_id, String track_id, Timestamp date) {
        super();
        this.session_id = session_id;
        this.track_id = track_id;
        this.date = date;
    }


    public PlayedSongs(long id, String session_id, String track_id, Timestamp date) {
        super();
        this.id = id;
        this.session_id = session_id;
        this.track_id = track_id;
        this.date = date;
    }

    public PlayedSongs(String session_id, String track_id) {
        super();
        this.session_id = session_id;
        this.track_id = track_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getTrack_id() {
        return track_id;
    }

    public void setTrack_id(String track_id) {
        this.track_id = track_id;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getPart_of_day() {
        return part_of_day;
    }

    public void setPart_of_day(String part_of_day) {
        this.part_of_day = part_of_day;
    }
}
