package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.Artista;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {
	Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}

