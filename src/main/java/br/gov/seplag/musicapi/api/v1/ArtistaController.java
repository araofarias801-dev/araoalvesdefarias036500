package br.gov.seplag.musicapi.api.v1;

import br.gov.seplag.musicapi.api.v1.dto.ArtistaRequest;
import br.gov.seplag.musicapi.api.v1.dto.ArtistaResponse;
import br.gov.seplag.musicapi.service.ArtistaService;
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
public class ArtistaController {
	private final ArtistaService artistaService;

	public ArtistaController(ArtistaService artistaService) {
		this.artistaService = artistaService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ArtistaResponse criar(@RequestBody ArtistaRequest request) {
		return artistaService.criar(request);
	}

	@PutMapping("/{id}")
	public ArtistaResponse atualizar(@PathVariable Long id, @RequestBody ArtistaRequest request) {
		return artistaService.atualizar(id, request);
	}

	@GetMapping("/{id}")
	public ArtistaResponse buscarPorId(@PathVariable Long id) {
		return artistaService.buscarPorId(id);
	}

	@GetMapping
	public Page<ArtistaResponse> listar(
		@RequestParam(name = "nome", required = false) String nome,
		@RequestParam(name = "ordem", required = false) String ordem,
		@RequestParam(name = "pagina", defaultValue = "0") int pagina,
		@RequestParam(name = "tamanho", defaultValue = "20") int tamanho
	) {
		return artistaService.listar(nome, ordem, pagina, tamanho);
	}
}
