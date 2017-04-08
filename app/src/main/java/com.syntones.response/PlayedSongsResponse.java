package com.syntones.response;

import com.syntones.model.Message;
import com.syntones.model.PlayedSongs;

import java.util.List;

public class PlayedSongsResponse {
	
	private Message message;
	private List<PlayedSongs> playedSongsList;
	
	public PlayedSongsResponse() {
		super();
	}

	public PlayedSongsResponse(Message message, List<PlayedSongs> playedSongsList) {
		super();
		this.message = message;
		this.playedSongsList = playedSongsList;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public List<PlayedSongs> getPlayedSongsList() {
		return playedSongsList;
	}

	public void setPlayedSongsList(List<PlayedSongs> playedSongsList) {
		this.playedSongsList = playedSongsList;
	}
	
	
	
	

}
