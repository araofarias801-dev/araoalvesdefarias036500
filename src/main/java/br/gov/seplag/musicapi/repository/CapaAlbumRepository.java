package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.CapaAlbum;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapaAlbumRepository extends JpaRepository<CapaAlbum, Long> {
	List<CapaAlbum> findAllByAlbumIdOrderByIdDesc(Long albumId);

	Optional<CapaAlbum> findTopByAlbumIdOrderByIdDesc(Long albumId);
}
