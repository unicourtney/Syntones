package com.syntones.model;

/**
 * Created by Courtney Love on 9/1/2016.
 */
public class Message {

    private String message;
    private String stackTrace;
    private boolean flag;


    private long counter = 0;

    //constructor
    public Message(){}
    public Message(String message){
        this.message = message;
    }
    public Message(String message,boolean flag){
        this.message = message;
        this.flag = flag;
    }
    public Message(String message,String stackTrace,boolean flag){
        this.message = message;
        this.stackTrace = stackTrace;
        this.flag = flag;
    }

    public Message(long counter,String message){
        this.counter = counter;
        this.message = message;
    }
    public Message(long counter, String message,boolean flag){
        this.message = message;
        this.counter = counter;
        this.flag = flag;
    }




    //getters and setters
    public void setFlag(boolean flag){
        this.flag = flag;
    }
    public boolean getFlag(){
        return flag;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public long getCounter() {
        return counter;
    }
    public void setCounter(long counter) {
        this.counter = counter;
    }
    public void setStackTrace(String stackTrace){
        this.stackTrace = stackTrace;
    }
    public String getStackTrace(){
        return this.stackTrace;
    }
}
