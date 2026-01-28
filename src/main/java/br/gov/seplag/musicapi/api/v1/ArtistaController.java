package br.gov.seplag.musicapi.api.v1;

import br.gov.seplag.musicapi.api.v1.dto.ArtistaRequest;
import br.gov.seplag.musicapi.api.v1.dto.ArtistaResponse;
import br.gov.seplag.musicapi.service.ArtistaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/artistas")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Artistas", description = "Operações de cadastro e consulta de artistas.")
public class ArtistaController {
	private final ArtistaService artistaService;

	public ArtistaController(ArtistaService artistaService) {
		this.artistaService = artistaService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Criar artista", description = "Cria um novo artista.")
	public ArtistaResponse criar(@RequestBody ArtistaRequest request) {
		return artistaService.criar(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar artista", description = "Atualiza os dados de um artista pelo id.")
	public ArtistaResponse atualizar(@PathVariable Long id, @RequestBody ArtistaRequest request) {
		return artistaService.atualizar(id, request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar artista por id", description = "Retorna um artista pelo id.")
	public ArtistaResponse buscarPorId(@PathVariable Long id) {
		return artistaService.buscarPorId(id);
	}

	@GetMapping
	@Operation(
		summary = "Listar artistas",
		description = "Lista artistas com paginação, filtro opcional por nome e ordenação asc/desc."
	)
	public Page<ArtistaResponse> listar(
		@RequestParam(name = "nome", required = false) String nome,
		@RequestParam(name = "ordem", required = false) String ordem,
		@RequestParam(name = "pagina", defaultValue = "0") int pagina,
		@RequestParam(name = "tamanho", defaultValue = "20") int tamanho
	) {
		return artistaService.listar(nome, ordem, pagina, tamanho);
	}
}
