package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.Artista;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {
}

