package br.gov.seplag.musicapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "regional")
public class Regional {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "id_integrador", nullable = false)
	private Integer idIntegrador;

	@Column(name = "nome", nullable = false, length = 200)
	private String nome;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getIdIntegrador() {
		return idIntegrador;
	}

	public void setIdIntegrador(Integer idIntegrador) {
		this.idIntegrador = idIntegrador;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
}
