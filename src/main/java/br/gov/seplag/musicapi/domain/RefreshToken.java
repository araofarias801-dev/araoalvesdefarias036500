package br.gov.seplag.musicapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "token", nullable = false, length = 200, unique = true)
	private String token;

	@Column(name = "expira_em", nullable = false)
	private Instant expiraEm;

	@Column(name = "revogado", nullable = false)
	private boolean revogado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Instant getExpiraEm() {
		return expiraEm;
	}

	public void setExpiraEm(Instant expiraEm) {
		this.expiraEm = expiraEm;
	}

	public boolean isRevogado() {
		return revogado;
	}

	public void setRevogado(boolean revogado) {
		this.revogado = revogado;
	}
}
