package br.gov.seplag.musicapi.api.v1.dto;

import br.gov.seplag.musicapi.domain.ArtistaTipo;

public class ArtistaResponse {
	private Long id;
	private String nome;
	private ArtistaTipo tipo;

	public ArtistaResponse(Long id, String nome, ArtistaTipo tipo) {
		this.id = id;
		this.nome = nome;
		this.tipo = tipo;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public ArtistaTipo getTipo() {
		return tipo;
	}
}

