package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<Album, Long> {
	@Query("""
		select distinct al
		from Album al
		left join al.artistas ar
		where (:titulo = '' or upper(al.titulo) like upper(concat('%', :titulo, '%')))
		  and (:artistaNome = '' or upper(ar.nome) like upper(concat('%', :artistaNome, '%')))
		  and (:artistaId is null or ar.id = :artistaId)
		""")
	Page<Album> buscar(
		@Param("titulo") String titulo,
		@Param("artistaNome") String artistaNome,
		@Param("artistaId") Long artistaId,
		Pageable pageable
	);

	@Query("""
		select distinct al
		from Album al
		left join fetch al.artistas ar
		where al.id = :id
		""")
	Album buscarComArtistasPorId(@Param("id") Long id);
}

