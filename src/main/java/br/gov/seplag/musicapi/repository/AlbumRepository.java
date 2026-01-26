package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.Album;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
}

