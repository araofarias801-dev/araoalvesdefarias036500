package br.gov.seplag.musicapi.api.v1.dto;

public class ArtistaResponse {
	private Long id;
	private String nome;

	public ArtistaResponse(Long id, String nome) {
		this.id = id;
		this.nome = nome;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}
}

