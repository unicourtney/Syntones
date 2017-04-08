package com.syntones.response;


import com.syntones.model.Genre;
import com.syntones.model.Message;

import java.util.List;

public class GenreResponse {
	private Message message;

	private List<Genre> genre;



	public GenreResponse() {
		super();

	}


	public GenreResponse(Message message, List<Genre> genre) {
		super();
		this.message = message;
		this.genre = genre;
	}


	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public List<Genre> getGenre() {
		return genre;
	}

	public void setGenre(List<Genre> genre) {
		this.genre = genre;
	}


}
