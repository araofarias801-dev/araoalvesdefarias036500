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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.ratelimit.enabled=false")
@AutoConfigureMockMvc
@Transactional
class AlbumControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ArtistaRepository artistaRepository;

	@Autowired
	private AlbumRepository albumRepository;

	@Autowired
	private CapaAlbumRepository capaAlbumRepository;

	@MockBean
	private MinioClient minioClient;

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
		org.junit.jupiter.api.Assertions.assertTrue(capaAlbumRepository.findByAlbumId(albumId).isPresent());
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
