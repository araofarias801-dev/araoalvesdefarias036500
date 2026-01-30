package br.gov.seplag.musicapi.api.v1.dto;

public class RegionalResponse {
	private Long id;
	private Integer idIntegrador;
	private String nome;
	private boolean ativo;

	public RegionalResponse(Long id, Integer idIntegrador, String nome, boolean ativo) {
		this.id = id;
		this.idIntegrador = idIntegrador;
		this.nome = nome;
		this.ativo = ativo;
	}

	public Long getId() {
		return id;
	}

	public Integer getIdIntegrador() {
		return idIntegrador;
	}

	public String getNome() {
		return nome;
	}

	public boolean isAtivo() {
		return ativo;
	}
}
