package br.gov.seplag.musicapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.seplag.musicapi.api.v1.dto.RegionalResponse;
import br.gov.seplag.musicapi.api.v1.dto.SincronizarRegionaisResponse;
import br.gov.seplag.musicapi.domain.Regional;
import br.gov.seplag.musicapi.repository.RegionalRepository;
import br.gov.seplag.musicapi.service.RegionaisIntegradorClient.RegionalIntegradorResponse;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class RegionalServiceUnitTests {
	@Mock
	private RegionalRepository regionalRepository;

	@Mock
	private RegionaisIntegradorClient regionaisIntegradorClient;

	@InjectMocks
	private RegionalService regionalService;

	@Test
	void listarSemFiltroNomeRetornaAtivasPorPadrao() {
		when(regionalRepository.findByAtivoTrue()).thenReturn(List.of(
			regional(10L, 1, "B", true),
			regional(11L, 2, "A", true)
		));

		List<RegionalResponse> response = regionalService.listar(null, null);

		assertThat(response).hasSize(2);
		assertThat(response.getFirst().getNome()).isEqualTo("A");
		assertThat(response.get(1).getNome()).isEqualTo("B");
		verify(regionalRepository).findByAtivoTrue();
		verify(regionalRepository, never()).findByAtivoFalse();
	}

	@Test
	void listarComAtivoFalseBuscaInativas() {
		when(regionalRepository.findByAtivoFalse()).thenReturn(List.of(
			regional(20L, 1, "X", false)
		));

		List<RegionalResponse> response = regionalService.listar(false, null);

		assertThat(response).hasSize(1);
		assertThat(response.getFirst().isAtivo()).isFalse();
		verify(regionalRepository).findByAtivoFalse();
		verify(regionalRepository, never()).findByAtivoTrue();
	}

	@Test
	void listarComNomeUsaBuscaCaseInsensitiveETrim() {
		when(regionalRepository.findByAtivoAndNomeContainingIgnoreCase(true, "mi")).thenReturn(List.of(
			regional(30L, 99, "Minas", true)
		));

		List<RegionalResponse> response = regionalService.listar(true, "  mi  ");

		assertThat(response).hasSize(1);
		assertThat(response.getFirst().getNome()).isEqualTo("Minas");
		verify(regionalRepository).findByAtivoAndNomeContainingIgnoreCase(true, "mi");
	}

	@Test
	void sincronizarInsereNovoQuandoNaoExisteAtivo() {
		when(regionaisIntegradorClient.listarRegionais()).thenReturn(List.of(
			integrador(1, "Regional A")
		));

		when(regionalRepository.findFirstByIdIntegradorAndAtivoTrue(1)).thenReturn(Optional.empty());
		when(regionalRepository.findByAtivoTrueAndIdIntegradorNotIn(anyCollection())).thenReturn(List.of());
		when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(regionalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		SincronizarRegionaisResponse response = regionalService.sincronizar();

		assertThat(response.getInseridos()).isEqualTo(1);
		assertThat(response.getInativados()).isEqualTo(0);
		verify(regionalRepository, times(1)).save(any(Regional.class));
		verify(regionalRepository, times(1)).findByAtivoTrueAndIdIntegradorNotIn(anyCollection());
	}

	@Test
	void sincronizarQuandoIntegradorFalhaRetorna502() {
		when(regionaisIntegradorClient.listarRegionais()).thenThrow(new RuntimeException("falha"));

		assertThatThrownBy(() -> regionalService.sincronizar())
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
				assertThat(ex.getReason()).isEqualTo("falha ao consultar integrador de regionais");
			});

		verify(regionalRepository, never()).findByAtivoTrue();
		verify(regionalRepository, never()).findByAtivoFalse();
		verify(regionalRepository, never()).findByAtivoTrueAndIdIntegradorNotIn(anyCollection());
		verify(regionalRepository, never()).save(any(Regional.class));
		verify(regionalRepository, never()).saveAll(anyList());
	}

	@Test
	void sincronizarInativaAusenteDoEndpoint() {
		when(regionaisIntegradorClient.listarRegionais()).thenReturn(List.of(
			integrador(1, "Regional A")
		));

		when(regionalRepository.findFirstByIdIntegradorAndAtivoTrue(1)).thenReturn(Optional.of(
			regional(100L, 1, "Regional A", true)
		));

		List<Regional> paraInativar = List.of(
			regional(101L, 2, "Regional B", true)
		);
		when(regionalRepository.findByAtivoTrueAndIdIntegradorNotIn(anyCollection())).thenReturn(paraInativar);
		when(regionalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		SincronizarRegionaisResponse response = regionalService.sincronizar();

		assertThat(response.getInseridos()).isEqualTo(0);
		assertThat(response.getInativados()).isEqualTo(1);

		ArgumentCaptor<Collection<Integer>> captor = ArgumentCaptor.forClass(Collection.class);
		verify(regionalRepository).findByAtivoTrueAndIdIntegradorNotIn(captor.capture());
		assertThat(captor.getValue()).containsExactly(1);

		ArgumentCaptor<List<Regional>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
		verify(regionalRepository).saveAll(saveAllCaptor.capture());
		assertThat(saveAllCaptor.getValue()).hasSize(1);
		assertThat(saveAllCaptor.getValue().getFirst().isAtivo()).isFalse();
	}

	@Test
	void sincronizarQuandoNomeMudaInativaAtualECriaNovo() {
		when(regionaisIntegradorClient.listarRegionais()).thenReturn(List.of(
			integrador(1, "Regional A (novo)")
		));

		Regional atual = regional(200L, 1, "Regional A", true);
		when(regionalRepository.findFirstByIdIntegradorAndAtivoTrue(1)).thenReturn(Optional.of(atual));
		when(regionalRepository.findByAtivoTrueAndIdIntegradorNotIn(anyCollection())).thenReturn(List.of());
		when(regionalRepository.save(any(Regional.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(regionalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		SincronizarRegionaisResponse response = regionalService.sincronizar();

		assertThat(response.getInseridos()).isEqualTo(1);
		assertThat(response.getInativados()).isEqualTo(1);

		ArgumentCaptor<Regional> saveCaptor = ArgumentCaptor.forClass(Regional.class);
		verify(regionalRepository, times(2)).save(saveCaptor.capture());
		List<Regional> salvos = saveCaptor.getAllValues();

		Regional inativado = salvos.getFirst();
		Regional novo = salvos.get(1);

		assertThat(inativado.getId()).isEqualTo(200L);
		assertThat(inativado.isAtivo()).isFalse();

		assertThat(novo.getId()).isNull();
		assertThat(novo.getIdIntegrador()).isEqualTo(1);
		assertThat(novo.getNome()).isEqualTo("Regional A (novo)");
		assertThat(novo.isAtivo()).isTrue();
	}

	@Test
	void sincronizarComEndpointVazioInativaTodosAtivos() {
		when(regionaisIntegradorClient.listarRegionais()).thenReturn(List.of());

		List<Regional> ativos = List.of(
			regional(300L, 1, "A", true),
			regional(301L, 2, "B", true)
		);
		when(regionalRepository.findByAtivoTrue()).thenReturn(ativos);
		when(regionalRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		SincronizarRegionaisResponse response = regionalService.sincronizar();

		assertThat(response.getInseridos()).isEqualTo(0);
		assertThat(response.getInativados()).isEqualTo(2);

		ArgumentCaptor<List<Regional>> captor = ArgumentCaptor.forClass(List.class);
		verify(regionalRepository).saveAll(captor.capture());
		assertThat(captor.getValue()).allMatch(r -> !r.isAtivo());
		verify(regionalRepository, never()).findByAtivoTrueAndIdIntegradorNotIn(anyCollection());
		verify(regionalRepository, never()).save(any(Regional.class));
	}

	private Regional regional(Long id, int idIntegrador, String nome, boolean ativo) {
		Regional r = new Regional();
		r.setId(id);
		r.setIdIntegrador(idIntegrador);
		r.setNome(nome);
		r.setAtivo(ativo);
		return r;
	}

	private RegionalIntegradorResponse integrador(int id, String nome) {
		RegionalIntegradorResponse r = new RegionalIntegradorResponse();
		r.setId(id);
		r.setNome(nome);
		return r;
	}
}
