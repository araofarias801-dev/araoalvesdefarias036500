package br.gov.seplag.musicapi.api.v1;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.gov.seplag.musicapi.repository.RegionalRepository;
import br.gov.seplag.musicapi.service.RegionaisIntegradorClient;
import br.gov.seplag.musicapi.service.RegionaisIntegradorClient.RegionalIntegradorResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = { "app.ratelimit.enabled=false", "spring.profiles.active=local" })
@AutoConfigureMockMvc
class RegionalControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RegionalRepository regionalRepository;

	@MockBean
	private RegionaisIntegradorClient regionaisIntegradorClient;

	@BeforeEach
	void setup() {
		regionalRepository.deleteAll();
	}

	@Test
	void sincronizaInsereInativaEVersionaAlteracao() throws Exception {
		when(regionaisIntegradorClient.listarRegionais()).thenReturn(List.of(
			regional(1, "Regional A"),
			regional(2, "Regional B")
		));

		mockMvc.perform(post("/v1/regionais/sincronizar").with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.inseridos").value(2))
			.andExpect(jsonPath("$.inativados").value(0));

		mockMvc.perform(get("/v1/regionais").with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));

		when(regionaisIntegradorClient.listarRegionais()).thenReturn(List.of(
			regional(1, "Regional A")
		));

		mockMvc.perform(post("/v1/regionais/sincronizar").with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.inseridos").value(0))
			.andExpect(jsonPath("$.inativados").value(1));

		mockMvc.perform(get("/v1/regionais").with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].idIntegrador").value(1))
			.andExpect(jsonPath("$[0].nome").value("Regional A"))
			.andExpect(jsonPath("$[0].ativo").value(true));

		mockMvc.perform(get("/v1/regionais").with(jwt()).param("ativo", "false"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].idIntegrador").value(2))
			.andExpect(jsonPath("$[0].ativo").value(false));

		when(regionaisIntegradorClient.listarRegionais()).thenReturn(List.of(
			regional(1, "Regional A (novo)")
		));

		mockMvc.perform(post("/v1/regionais/sincronizar").with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.inseridos").value(1))
			.andExpect(jsonPath("$.inativados").value(1));

		mockMvc.perform(get("/v1/regionais").with(jwt()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].idIntegrador").value(1))
			.andExpect(jsonPath("$[0].nome").value("Regional A (novo)"))
			.andExpect(jsonPath("$[0].ativo").value(true));

		mockMvc.perform(get("/v1/regionais").with(jwt()).param("ativo", "false"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2));
	}

	private RegionalIntegradorResponse regional(int id, String nome) {
		RegionalIntegradorResponse r = new RegionalIntegradorResponse();
		r.setId(id);
		r.setNome(nome);
		return r;
	}
}
