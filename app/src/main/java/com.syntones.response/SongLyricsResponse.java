package com.syntones.response;


import com.syntones.model.Message;

public class SongLyricsResponse {

	private Message message;
	private String lyrics;

	public SongLyricsResponse() {
		super();

	}

	public SongLyricsResponse(Message message, String lyrics) {
		super();
		this.message = message;
		this.lyrics = lyrics;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}

}
