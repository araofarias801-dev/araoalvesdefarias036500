package br.gov.seplag.musicapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.seplag.musicapi.api.v1.dto.AlbumRequest;
import br.gov.seplag.musicapi.api.v1.dto.AlbumResponse;
import br.gov.seplag.musicapi.domain.Album;
import br.gov.seplag.musicapi.domain.Artista;
import br.gov.seplag.musicapi.repository.AlbumRepository;
import br.gov.seplag.musicapi.repository.ArtistaRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AlbumServiceUnitTests {
	@Mock
	private AlbumRepository albumRepository;

	@Mock
	private ArtistaRepository artistaRepository;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@InjectMocks
	private AlbumService albumService;

	@Test
	void criarSemTituloRetorna400() {
		AlbumRequest request = new AlbumRequest();
		request.setTitulo("   ");

		assertThatThrownBy(() -> albumService.criar(request))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(ex.getReason()).isEqualTo("titulo é obrigatório");
			});

		verify(albumRepository, never()).save(any(Album.class));
		verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
	}

	@Test
	void criarSemArtistasSalvaBuscaEnviaMensagem() {
		AlbumRequest request = new AlbumRequest();
		request.setTitulo("  OK  ");

		when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> {
			Album album = invocation.getArgument(0);
			album.setId(1L);
			return album;
		});
		when(albumRepository.buscarComArtistasPorId(1L)).thenReturn(albumComArtistas(1L, "OK", List.of()));

		AlbumResponse response = albumService.criar(request);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitulo()).isEqualTo("OK");
		verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/albuns"), any(AlbumResponse.class));
		verify(artistaRepository, never()).saveAll(any());
	}

	@Test
	void criarComArtistaIdNullRetorna400() {
		AlbumRequest request = new AlbumRequest();
		request.setTitulo("OK");
		request.setArtistaIds(Arrays.asList(1L, null));

		when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> {
			Album album = invocation.getArgument(0);
			album.setId(10L);
			return album;
		});

		assertThatThrownBy(() -> albumService.criar(request))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(ex.getReason()).isEqualTo("artistaIds não pode conter null");
			});

		verify(artistaRepository, never()).saveAll(any());
		verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
	}

	@Test
	void listarNormalizaFiltrosParaVazioEOrdenaPorTitulo() {
		when(albumRepository.buscar(eq("x"), eq("y"), eq(null), eq(null), eq(null), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of(
				albumComArtistas(1L, "t1", List.of())
			)));

		albumService.listar(" x ", " y ", null, null, null, "desc", 2, 20);

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(albumRepository).buscar(eq("x"), eq("y"), eq(null), eq(null), eq(null), captor.capture());
		Direction direction = captor.getValue().getSort().getOrderFor("titulo").getDirection();
		assertThat(direction).isEqualTo(Direction.DESC);
		assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
		assertThat(captor.getValue().getPageSize()).isEqualTo(20);
	}

	@Test
	void atualizarComArtistasAtualizaRelacionamentos() {
		Album album = new Album();
		album.setId(5L);
		album.setTitulo("T");

		Album antigoLink = new Album();
		antigoLink.setId(5L);
		antigoLink.setTitulo("T");

		Artista antigo = artista(1L, "Old", Set.of(antigoLink));
		when(albumRepository.findById(5L)).thenReturn(Optional.of(album));
		when(artistaRepository.buscarPorAlbumId(5L)).thenReturn(List.of(antigo));
		when(albumRepository.save(any(Album.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Artista novo = artista(2L, "New", Set.of());
		when(artistaRepository.findAllById(any())).thenReturn(List.of(novo));
		when(artistaRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(albumRepository.buscarComArtistasPorId(5L)).thenReturn(albumComArtistas(5L, "T", List.of(novo)));

		AlbumRequest request = new AlbumRequest();
		request.setArtistaIds(List.of(2L));

		AlbumResponse response = albumService.atualizar(5L, request);

		assertThat(response.getArtistas()).extracting("id").containsExactly(2L);
		assertThat(antigo.getAlbuns()).isEmpty();
		verify(artistaRepository, times(2)).saveAll(any());
	}

	@Test
	void buscarPorIdQuandoNaoEncontradoRetorna404() {
		when(albumRepository.buscarComArtistasPorId(anyLong())).thenReturn(null);

		assertThatThrownBy(() -> albumService.buscarPorId(99L))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getReason()).isEqualTo("álbum não encontrado");
			});
	}

	private static Album albumComArtistas(Long id, String titulo, List<Artista> artistas) {
		Album album = new Album();
		album.setId(id);
		album.setTitulo(titulo);
		album.getArtistas().addAll(artistas);
		for (Artista artista : artistas) {
			artista.getAlbuns().add(album);
		}
		return album;
	}

	private static Artista artista(Long id, String nome, Set<Album> albuns) {
		Artista artista = new Artista();
		artista.setId(id);
		artista.setNome(nome);
		artista.getAlbuns().clear();
		artista.getAlbuns().addAll(albuns);
		return artista;
	}
}
