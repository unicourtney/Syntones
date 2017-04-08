package com.syntones.model;




public class Genre {


    private long id;


    private String genre;


    public Genre() {


    }


    public Genre(long id, String genre) {
        super();
        this.id = id;
        this.genre = genre;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getGenre() {
        return genre;
    }


    public void setGenre(String genre) {
        this.genre = genre;
    }


}
