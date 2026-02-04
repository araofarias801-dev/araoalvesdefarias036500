package br.gov.seplag.musicapi.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import br.gov.seplag.musicapi.repository.AlbumRepository;
import br.gov.seplag.musicapi.repository.ArtistaRepository;
import br.gov.seplag.musicapi.repository.CapaAlbumRepository;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = { "app.ratelimit.enabled=false", "spring.profiles.active=local" })
@AutoConfigureMockMvc
class AlbumControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ArtistaRepository artistaRepository;

	@Autowired
	private AlbumRepository albumRepository;

	@Autowired
	private CapaAlbumRepository capaAlbumRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockBean
	private MinioClient minioClient;

	@MockBean
	private SimpMessagingTemplate messagingTemplate;

	@BeforeEach
	void setup() {
		capaAlbumRepository.deleteAll();
		jdbcTemplate.update("delete from artista_album");
		albumRepository.deleteAll();
		artistaRepository.deleteAll();
	}

	@Test
	void criaEBuscaAlbum() throws Exception {
		mockMvc.perform(post("/v1/artistas")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Serj Tankian\"}"))
			.andExpect(status().isCreated());

		Long artistaId = artistaRepository.findAll().getFirst().getId();

		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Harakiri\",\"artistaIds\":[" + artistaId + "]}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.titulo").value("Harakiri"))
			.andExpect(jsonPath("$.artistas.length()").value(1))
			.andExpect(jsonPath("$.artistas[0].id").value(artistaId))
			.andExpect(jsonPath("$.artistas[0].nome").value("Serj Tankian"));

		Mockito.verify(messagingTemplate).convertAndSend(Mockito.eq("/topic/albuns"), Mockito.any(Object.class));

		Long albumId = albumRepository.findAll().getFirst().getId();

		mockMvc.perform(get("/v1/albuns/{id}", albumId).with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(albumId))
			.andExpect(jsonPath("$.titulo").value("Harakiri"))
			.andExpect(jsonPath("$.artistas[0].nome").value("Serj Tankian"));
	}

	@Test
	void atualizaAlbum() throws Exception {
		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Old\"}"))
			.andExpect(status().isCreated());

		Long albumId = albumRepository.findAll().getFirst().getId();

		mockMvc.perform(put("/v1/albuns/{id}", albumId)
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"New\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(albumId))
			.andExpect(jsonPath("$.titulo").value("New"));
	}

	@Test
	void listaAlbunsPaginados() throws Exception {
		mockMvc.perform(post("/v1/albuns")
			.with(jwt())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"titulo\":\"B\"}")).andExpect(status().isCreated());

		mockMvc.perform(post("/v1/albuns")
			.with(jwt())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"titulo\":\"A\"}")).andExpect(status().isCreated());

		mockMvc.perform(get("/v1/albuns")
			.with(jwt())
			.param("ordem", "asc")
			.param("pagina", "0")
			.param("tamanho", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(2))
			.andExpect(jsonPath("$.size").value(1))
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].titulo").value("A"));
	}

	@Test
	void filtraAlbunsPorNomeDoArtista() throws Exception {
		mockMvc.perform(post("/v1/artistas")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Mike Shinoda\"}"))
			.andExpect(status().isCreated());

		Long artistaId = artistaRepository.findAll().getFirst().getId();

		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Post Traumatic\",\"artistaIds\":[" + artistaId + "]}"))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Outro\"}"))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/v1/albuns").with(jwt()).param("artistaNome", "Mike"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].titulo").value("Post Traumatic"));
	}

	@Test
	void filtraAlbunsPorTipoDeArtista() throws Exception {
		mockMvc.perform(post("/v1/artistas")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Guns N' Roses\",\"tipo\":\"BANDA\"}"))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/v1/artistas")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Serj Tankian\",\"tipo\":\"CANTOR\"}"))
			.andExpect(status().isCreated());

		Long bandaId = artistaRepository.findAll().stream()
			.filter(a -> "Guns N' Roses".equals(a.getNome()))
			.findFirst()
			.orElseThrow()
			.getId();

		Long cantorId = artistaRepository.findAll().stream()
			.filter(a -> "Serj Tankian".equals(a.getNome()))
			.findFirst()
			.orElseThrow()
			.getId();

		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Album Banda\",\"artistaIds\":[" + bandaId + "]}"))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Album Cantor\",\"artistaIds\":[" + cantorId + "]}"))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/v1/albuns").with(jwt()).param("temBanda", "true"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].titulo").value("Album Banda"));

		mockMvc.perform(get("/v1/albuns").with(jwt()).param("temCantor", "true"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].titulo").value("Album Cantor"));
	}

	@Test
	void filtraAlbunsPorTitulo() throws Exception {
		mockMvc.perform(post("/v1/albuns")
			.with(jwt())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"titulo\":\"Post Traumatic\"}")).andExpect(status().isCreated());

		mockMvc.perform(post("/v1/albuns")
			.with(jwt())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"titulo\":\"Outro\"}")).andExpect(status().isCreated());

		mockMvc.perform(get("/v1/albuns").with(jwt()).param("titulo", "post"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].titulo").value("Post Traumatic"));
	}

	@Test
	void rejeitaCriacaoDeAlbumComArtistaInexistente() throws Exception {
		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Qualquer\",\"artistaIds\":[999999]}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void rejeitaAtualizacaoDeAlbumComArtistaInexistente() throws Exception {
		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Old\"}"))
			.andExpect(status().isCreated());

		Long albumId = albumRepository.findAll().getFirst().getId();

		mockMvc.perform(put("/v1/albuns/{id}", albumId)
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"artistaIds\":[999999]}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void enviaCapaEGeraUrl() throws Exception {
		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Com capa\"}"))
			.andExpect(status().isCreated());

		Long albumId = albumRepository.findAll().getFirst().getId();

		Mockito.when(minioClient.bucketExists(Mockito.any())).thenReturn(true);
		ObjectWriteResponse resposta = Mockito.mock(ObjectWriteResponse.class);
		Mockito.when(resposta.etag()).thenReturn("etag");
		Mockito.when(minioClient.putObject(Mockito.any())).thenReturn(resposta);
		Mockito.when(minioClient.getPresignedObjectUrl(Mockito.any())).thenReturn("http://presigned");

		MockMultipartFile arquivo = new MockMultipartFile(
			"arquivo",
			"capa.png",
			"image/png",
			"conteudo".getBytes()
		);

		mockMvc.perform(multipart("/v1/albuns/{id}/capa", albumId).file(arquivo).with(jwt()))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/v1/albuns/{id}/capa/url", albumId).with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.url").value("http://presigned"));

		Mockito.verify(minioClient).putObject(Mockito.any());
		Mockito.verify(minioClient).getPresignedObjectUrl(Mockito.any());
		org.junit.jupiter.api.Assertions.assertTrue(capaAlbumRepository.findTopByAlbumIdOrderByIdDesc(albumId).isPresent());
	}

	@Test
	void enviaMultiplasCapasEGeraUrls() throws Exception {
		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Com capas\"}"))
			.andExpect(status().isCreated());

		Long albumId = albumRepository.findAll().getFirst().getId();

		Mockito.when(minioClient.bucketExists(Mockito.any())).thenReturn(true);
		ObjectWriteResponse resposta = Mockito.mock(ObjectWriteResponse.class);
		Mockito.when(resposta.etag()).thenReturn("etag");
		Mockito.when(minioClient.putObject(Mockito.any())).thenReturn(resposta);
		Mockito.when(minioClient.getPresignedObjectUrl(Mockito.any())).thenReturn("http://presigned");

		MockMultipartFile arquivo1 = new MockMultipartFile(
			"arquivos",
			"capa1.png",
			"image/png",
			"conteudo1".getBytes()
		);
		MockMultipartFile arquivo2 = new MockMultipartFile(
			"arquivos",
			"capa2.png",
			"image/png",
			"conteudo2".getBytes()
		);

		mockMvc.perform(multipart("/v1/albuns/{id}/capa", albumId).file(arquivo1).file(arquivo2).with(jwt()))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/v1/albuns/{id}/capa/urls", albumId).with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.urls.length()").value(2))
			.andExpect(jsonPath("$.urls[0]").value("http://presigned"))
			.andExpect(jsonPath("$.urls[1]").value("http://presigned"));

		List<?> capas = capaAlbumRepository.findAllByAlbumIdOrderByIdDesc(albumId);
		org.junit.jupiter.api.Assertions.assertEquals(2, capas.size());
		Mockito.verify(minioClient, Mockito.times(2)).putObject(Mockito.any());
		Mockito.verify(minioClient, Mockito.times(2)).getPresignedObjectUrl(Mockito.any());
	}

	@Test
	void rejeitaEnvioDeCapaComTipoInvalido() throws Exception {
		mockMvc.perform(post("/v1/albuns")
				.with(jwt())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"titulo\":\"Sem capa\"}"))
			.andExpect(status().isCreated());

		Long albumId = albumRepository.findAll().getFirst().getId();

		MockMultipartFile arquivo = new MockMultipartFile(
			"arquivo",
			"capa.txt",
			"text/plain",
			"conteudo".getBytes()
		);

		mockMvc.perform(multipart("/v1/albuns/{id}/capa", albumId).file(arquivo).with(jwt()))
			.andExpect(status().isUnsupportedMediaType());
	}
}
