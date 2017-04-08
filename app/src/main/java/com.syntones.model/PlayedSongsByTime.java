package com.syntones.model;

import java.sql.Timestamp;

public class PlayedSongsByTime {


    private long id;

    private String track_id;

    private int midnight;

    private int morning;

    private int noon;

    private int afternoon;

    private int evening;


    public PlayedSongsByTime() {
        super();

    }

    public PlayedSongsByTime(String track_id, int midnight, int morning, int noon, int afternoon, int evening) {
        super();
        this.track_id = track_id;
        this.midnight = midnight;
        this.morning = morning;
        this.noon = noon;
        this.afternoon = afternoon;
        this.evening = evening;
    }

    public PlayedSongsByTime(long id, String track_id, int midnight, int morning, int noon, int afternoon,
                             int evening) {
        super();
        this.id = id;
        this.track_id = track_id;
        this.midnight = midnight;
        this.morning = morning;
        this.noon = noon;
        this.afternoon = afternoon;
        this.evening = evening;
    }


    public String getTrack_id() {
        return track_id;
    }

    public void setTrack_id(String track_id) {
        this.track_id = track_id;
    }

    public int getMidnight() {
        return midnight;
    }

    public void setMidnight(int midnight) {
        this.midnight = midnight;
    }

    public int getMorning() {
        return morning;
    }

    public void setMorning(int morning) {
        this.morning = morning;
    }

    public int getNoon() {
        return noon;
    }

    public void setNoon(int noon) {
        this.noon = noon;
    }

    public int getAfternoon() {
        return afternoon;
    }

    public void setAfternoon(int afternoon) {
        this.afternoon = afternoon;
    }

    public int getEvening() {
        return evening;
    }

    public void setEvening(int evening) {
        this.evening = evening;
    }


}
