package br.gov.seplag.musicapi.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@Tag(name = "Util", description = "Endpoints utilitários.")
public class PingController {
	@GetMapping("/ping")
	@Operation(summary = "Ping", description = "Endpoint público para verificar se a API está no ar.")
	public Map<String, String> ping() {
		return Map.of("status", "ok");
	}
}
