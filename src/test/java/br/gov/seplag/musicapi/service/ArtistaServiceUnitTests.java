package br.gov.seplag.musicapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.seplag.musicapi.api.v1.dto.ArtistaRequest;
import br.gov.seplag.musicapi.api.v1.dto.ArtistaResponse;
import br.gov.seplag.musicapi.domain.Artista;
import br.gov.seplag.musicapi.repository.ArtistaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
class ArtistaServiceUnitTests {
	@Mock
	private ArtistaRepository artistaRepository;

	@InjectMocks
	private ArtistaService artistaService;

	@Test
	void criarNormalizaNomeComTrim() {
		ArtistaRequest request = new ArtistaRequest();
		request.setNome("  Serj  ");

		when(artistaRepository.save(any(Artista.class))).thenAnswer(invocation -> {
			Artista a = invocation.getArgument(0);
			a.setId(10L);
			return a;
		});

		ArtistaResponse response = artistaService.criar(request);

		assertThat(response.getId()).isEqualTo(10L);
		assertThat(response.getNome()).isEqualTo("Serj");
	}

	@Test
	void atualizarBuscaSalvaERetornaResponse() {
		ArtistaRequest request = new ArtistaRequest();
		request.setNome("  A  ");

		Artista existente = new Artista();
		existente.setId(1L);
		existente.setNome("Old");

		when(artistaRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(artistaRepository.save(any(Artista.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArtistaResponse response = artistaService.atualizar(1L, request);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getNome()).isEqualTo("A");
	}

	@Test
	void listarSemNomeUsaFindAllComOrdenacaoPadraoAsc() {
		when(artistaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
			artista(1L, "B"),
			artista(2L, "A")
		)));

		Page<ArtistaResponse> page = artistaService.listar(null, null, 0, 10);

		assertThat(page.getTotalElements()).isEqualTo(2);
		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(artistaRepository).findAll(captor.capture());

		Direction direction = captor.getValue().getSort().getOrderFor("nome").getDirection();
		assertThat(direction).isEqualTo(Direction.ASC);
	}

	@Test
	void listarComNomeUsaBuscaCaseInsensitiveETrim() {
		when(artistaRepository.findByNomeContainingIgnoreCase(any(String.class), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of(artista(10L, "Teste"))));

		Page<ArtistaResponse> page = artistaService.listar("  te  ", "desc", 1, 5);

		assertThat(page.getContent()).hasSize(1);
		ArgumentCaptor<String> nomeCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(artistaRepository).findByNomeContainingIgnoreCase(nomeCaptor.capture(), pageableCaptor.capture());

		assertThat(nomeCaptor.getValue()).isEqualTo("te");
		Direction direction = pageableCaptor.getValue().getSort().getOrderFor("nome").getDirection();
		assertThat(direction).isEqualTo(Direction.DESC);
		assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
	}

	private static Artista artista(Long id, String nome) {
		Artista artista = new Artista();
		artista.setId(id);
		artista.setNome(nome);
		return artista;
	}
}
