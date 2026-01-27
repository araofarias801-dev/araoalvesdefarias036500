package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.Artista;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {
	Page<Artista> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

	@Query("""
		select a
		from Artista a
		join a.albuns al
		where al.id = :albumId
		""")
	List<Artista> buscarPorAlbumId(@Param("albumId") Long albumId);
}

