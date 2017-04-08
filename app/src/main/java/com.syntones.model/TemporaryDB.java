package com.syntones.model;


import java.sql.Timestamp;
import java.util.Date;

public class TemporaryDB {

    private Long id;

    private String session_id, part_of_day;

    private long song_id, user_id;

    private Timestamp date;

    public TemporaryDB() {
        super();
    }

    public TemporaryDB(String session_id, long song_id, Timestamp date, long user_id, String part_of_day) {
        super();
        this.session_id = session_id;
        this.song_id = song_id;
        this.date = date;
        this.user_id = user_id;
        this.part_of_day = part_of_day;
    }

    public TemporaryDB(long song_id, Timestamp date, String session_id, long user_id) {
        this.song_id = song_id;
        this.date = date;
        this.session_id = session_id;
        this.user_id = user_id;
    }

    public TemporaryDB(String session_id, long song_id, Timestamp date) {
        this.session_id = session_id;
        this.song_id = song_id;
        this.date = date;
    }

    public TemporaryDB(Long id, String session_id, long song_id) {
        super();
        this.id = id;
        this.session_id = session_id;
        this.song_id = song_id;
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

    public long getSong_id() {
        return song_id;
    }

    public void setSong_id(long song_id) {
        this.song_id = song_id;
    }

    public Date getDate() {
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
