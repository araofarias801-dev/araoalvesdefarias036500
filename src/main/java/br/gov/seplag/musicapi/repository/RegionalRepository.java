package br.gov.seplag.musicapi.repository;

import br.gov.seplag.musicapi.domain.Regional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionalRepository extends JpaRepository<Regional, Long> {
	Optional<Regional> findFirstByIdIntegradorAndAtivoTrue(Integer idIntegrador);

	List<Regional> findByAtivoTrue();

	List<Regional> findByAtivoFalse();

	List<Regional> findByAtivoAndNomeContainingIgnoreCase(boolean ativo, String nome);

	List<Regional> findByAtivoTrueAndIdIntegradorNotIn(Collection<Integer> idsIntegrador);
}
