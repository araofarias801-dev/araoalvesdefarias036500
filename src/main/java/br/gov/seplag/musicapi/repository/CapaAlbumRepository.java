package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.CapaAlbum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapaAlbumRepository extends JpaRepository<CapaAlbum, Long> {
	Optional<CapaAlbum> findByAlbumId(Long albumId);
}
