package br.gov.seplag.musicapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "artista")
public class Artista {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 200)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 20)
	private ArtistaTipo tipo;

	@ManyToMany
	@JoinTable(
		name = "artista_album",
		joinColumns = @JoinColumn(name = "artista_id"),
		inverseJoinColumns = @JoinColumn(name = "album_id")
	)
	private Set<Album> albuns = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Set<Album> getAlbuns() {
		return albuns;
	}

	public void setAlbuns(Set<Album> albuns) {
		this.albuns = albuns;
	}
}

