package br.gov.seplag.musicapi.api.v1.dto;

import java.util.List;

public class AlbumResponse {
	private Long id;
	private String titulo;
	private List<ArtistaResumoResponse> artistas;

	public AlbumResponse(Long id, String titulo, List<ArtistaResumoResponse> artistas) {
		this.id = id;
		this.titulo = titulo;
		this.artistas = artistas;
	}

	public Long getId() {
		return id;
	}

	public String getTitulo() {
		return titulo;
	}

	public List<ArtistaResumoResponse> getArtistas() {
		return artistas;
	}
}

