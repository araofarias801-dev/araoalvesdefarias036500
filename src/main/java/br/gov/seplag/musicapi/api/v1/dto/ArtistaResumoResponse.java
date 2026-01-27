package br.gov.seplag.musicapi.api.v1.dto;

public class ArtistaResumoResponse {
	private Long id;
	private String nome;

	public ArtistaResumoResponse(Long id, String nome) {
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

