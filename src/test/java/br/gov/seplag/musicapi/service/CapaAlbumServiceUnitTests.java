package br.gov.seplag.musicapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.seplag.musicapi.domain.CapaAlbum;
import br.gov.seplag.musicapi.repository.AlbumRepository;
import br.gov.seplag.musicapi.repository.CapaAlbumRepository;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CapaAlbumServiceUnitTests {
	@Mock
	private AlbumRepository albumRepository;

	@Mock
	private CapaAlbumRepository capaAlbumRepository;

	@Mock
	private MinioClient minioClient;

	private CapaAlbumService capaAlbumService;

	@BeforeEach
	void setup() {
		capaAlbumService = new CapaAlbumService(albumRepository, capaAlbumRepository, minioClient, "bucket-test");
	}

	@Test
	void enviarQuandoAlbumNaoExisteRetorna404() {
		when(albumRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> capaAlbumService.enviar(1L, arquivo("image/png", new byte[] { 1 })))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getReason()).isEqualTo("álbum não encontrado");
			});
	}

	@Test
	void enviarQuandoArquivoVazioRetorna400() {
		when(albumRepository.existsById(1L)).thenReturn(true);

		assertThatThrownBy(() -> capaAlbumService.enviar(1L, arquivo("image/png", new byte[] {})))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(ex.getReason()).isEqualTo("arquivo é obrigatório");
			});
	}

	@Test
	void enviarQuandoContentTypeNaoSuportadoRetorna415() {
		when(albumRepository.existsById(1L)).thenReturn(true);

		assertThatThrownBy(() -> capaAlbumService.enviar(1L, arquivo("application/pdf", new byte[] { 1 })))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
				assertThat(ex.getReason()).isEqualTo("tipo de arquivo não suportado");
			});
	}

	@Test
	void enviarCriaCapaESalvaNoRepositorio() throws Exception {
		when(albumRepository.existsById(10L)).thenReturn(true);
		when(minioClient.bucketExists(any())).thenReturn(false);
		ObjectWriteResponse objectWriteResponse = org.mockito.Mockito.mock(ObjectWriteResponse.class);
		when(objectWriteResponse.etag()).thenReturn("etag");
		when(minioClient.putObject(any())).thenReturn(objectWriteResponse);
		when(capaAlbumRepository.save(any(CapaAlbum.class))).thenAnswer(invocation -> invocation.getArgument(0));

		capaAlbumService.enviar(10L, arquivo("image/png", "abc".getBytes(StandardCharsets.UTF_8)));

		verify(minioClient).makeBucket(any());
		verify(minioClient, never()).removeObject(any());
		ArgumentCaptor<CapaAlbum> captor = ArgumentCaptor.forClass(CapaAlbum.class);
		verify(capaAlbumRepository).save(captor.capture());
		assertThat(captor.getValue().getAlbumId()).isEqualTo(10L);
		assertThat(captor.getValue().getBucket()).isEqualTo("bucket-test");
		assertThat(captor.getValue().getObjeto()).startsWith("albuns/10/capa-");
		assertThat(captor.getValue().getObjeto()).endsWith(".png");
		assertThat(captor.getValue().getContentType()).isEqualTo("image/png");
		assertThat(captor.getValue().getEtag()).isEqualTo("etag");
		assertThat(captor.getValue().getNomeOriginal()).isEqualTo("arquivo.png");
		assertThat(captor.getValue().getTamanho()).isEqualTo(3L);
	}

	@Test
	void gerarUrlQuandoNaoExisteCapaRetorna404() {
		when(albumRepository.existsById(anyLong())).thenReturn(true);
		when(capaAlbumRepository.findTopByAlbumIdOrderByIdDesc(anyLong())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> capaAlbumService.gerarUrlPorAlbumId(1L))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				assertThat(ex.getReason()).isEqualTo("capa não encontrada");
			});
	}

	@Test
	void gerarUrlRetornaPresignedUrl() throws Exception {
		when(albumRepository.existsById(anyLong())).thenReturn(true);
		CapaAlbum capa = new CapaAlbum();
		capa.setAlbumId(1L);
		capa.setBucket("bucket-1");
		capa.setObjeto("o1");
		when(capaAlbumRepository.findTopByAlbumIdOrderByIdDesc(1L)).thenReturn(Optional.of(capa));
		when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://url");

		String url = capaAlbumService.gerarUrlPorAlbumId(1L);

		assertThat(url).isEqualTo("http://url");
	}

	@Test
	void gerarUrlQuandoMinioFalhaRetorna502() throws Exception {
		when(albumRepository.existsById(anyLong())).thenReturn(true);
		CapaAlbum capa = new CapaAlbum();
		capa.setAlbumId(1L);
		capa.setBucket("bucket-1");
		capa.setObjeto("o1");
		when(capaAlbumRepository.findTopByAlbumIdOrderByIdDesc(1L)).thenReturn(Optional.of(capa));
		when(minioClient.getPresignedObjectUrl(any())).thenThrow(new RuntimeException("falha"));

		assertThatThrownBy(() -> capaAlbumService.gerarUrlPorAlbumId(1L))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
				assertThat(ex.getReason()).isEqualTo("falha ao gerar url da capa");
			});
	}

	@Test
	void gerarUrlsRetornaListaDePresignedUrls() throws Exception {
		when(albumRepository.existsById(anyLong())).thenReturn(true);
		CapaAlbum capa1 = new CapaAlbum();
		capa1.setAlbumId(1L);
		capa1.setBucket("bucket-1");
		capa1.setObjeto("o1");
		CapaAlbum capa2 = new CapaAlbum();
		capa2.setAlbumId(1L);
		capa2.setBucket("bucket-1");
		capa2.setObjeto("o2");

		when(capaAlbumRepository.findAllByAlbumIdOrderByIdDesc(1L)).thenReturn(List.of(capa2, capa1));
		when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://url");

		List<String> urls = capaAlbumService.gerarUrlsPorAlbumId(1L);

		assertThat(urls).containsExactly("http://url", "http://url");
	}

	private static MockMultipartFile arquivo(String contentType, byte[] bytes) {
		return new MockMultipartFile("arquivo", "arquivo.png", contentType, bytes);
	}
}
