package br.gov.seplag.musicapi.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.gov.seplag.musicapi.repository.ArtistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ArtistaControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ArtistaRepository artistaRepository;

	@BeforeEach
	void setup() {
		artistaRepository.deleteAll();
	}

	@Test
	void criaEBuscaArtista() throws Exception {
		mockMvc.perform(post("/v1/artistas")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Serj Tankian\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.nome").value("Serj Tankian"));

		Long id = artistaRepository.findAll().getFirst().getId();

		mockMvc.perform(get("/v1/artistas/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.nome").value("Serj Tankian"));
	}

	@Test
	void listaArtistasOrdenados() throws Exception {
		mockMvc.perform(post("/v1/artistas")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"nome\":\"B\"}")).andExpect(status().isCreated());

		mockMvc.perform(post("/v1/artistas")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"nome\":\"A\"}")).andExpect(status().isCreated());

		mockMvc.perform(get("/v1/artistas").param("ordem", "asc"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].nome").value("A"))
			.andExpect(jsonPath("$.content[1].nome").value("B"));
	}

	@Test
	void filtraArtistasPorNome() throws Exception {
		mockMvc.perform(post("/v1/artistas")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"nome\":\"Mike Shinoda\"}")).andExpect(status().isCreated());

		mockMvc.perform(post("/v1/artistas")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"nome\":\"Michel Teló\"}")).andExpect(status().isCreated());

		mockMvc.perform(get("/v1/artistas").param("nome", "Mike"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].nome").value("Mike Shinoda"));
	}

	@Test
	void paginaArtistas() throws Exception {
		mockMvc.perform(post("/v1/artistas")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"nome\":\"A\"}")).andExpect(status().isCreated());

		mockMvc.perform(post("/v1/artistas")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"nome\":\"B\"}")).andExpect(status().isCreated());

		mockMvc.perform(get("/v1/artistas")
			.param("ordem", "asc")
			.param("pagina", "0")
			.param("tamanho", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalElements").value(2))
			.andExpect(jsonPath("$.size").value(1))
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].nome").value("A"));
	}
}
