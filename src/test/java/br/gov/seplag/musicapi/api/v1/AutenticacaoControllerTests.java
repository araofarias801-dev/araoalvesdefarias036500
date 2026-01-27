package br.gov.seplag.musicapi.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "app.ratelimit.enabled=false")
@AutoConfigureMockMvc
@Transactional
class AutenticacaoControllerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void exigeAutenticacaoNoV1() throws Exception {
		mockMvc.perform(get("/v1/artistas"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void cadastraLoginEUsaToken() throws Exception {
		mockMvc.perform(post("/v1/autenticacao/cadastrar")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"user1\",\"senha\":\"senha123\"}"))
			.andExpect(status().isCreated());

		MvcResult result = mockMvc.perform(post("/v1/autenticacao/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"user1\",\"senha\":\"senha123\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString())
			.andExpect(jsonPath("$.refreshToken").isString())
			.andReturn();

		Map<?, ?> body = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
		String accessToken = String.valueOf(body.get("accessToken"));
		String refreshToken = String.valueOf(body.get("refreshToken"));

		mockMvc.perform(post("/v1/artistas")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Teste\"}"))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/v1/autenticacao/renovar")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isString())
			.andExpect(jsonPath("$.refreshToken").isString());
	}
}
