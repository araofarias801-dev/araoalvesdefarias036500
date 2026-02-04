package br.gov.seplag.musicapi.api.v1.dto;

import br.gov.seplag.musicapi.domain.ArtistaTipo;

public class ArtistaRequest {
	private String nome;
	private ArtistaTipo tipo;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public ArtistaTipo getTipo() {
		return tipo;
	}

	public void setTipo(ArtistaTipo tipo) {
		this.tipo = tipo;
	}
}

