package br.gov.seplag.musicapi.api.v1.dto;

import java.util.List;

public class AlbumRequest {
	private String titulo;
	private List<Long> artistaIds;

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public List<Long> getArtistaIds() {
		return artistaIds;
	}

	public void setArtistaIds(List<Long> artistaIds) {
		this.artistaIds = artistaIds;
	}
}

