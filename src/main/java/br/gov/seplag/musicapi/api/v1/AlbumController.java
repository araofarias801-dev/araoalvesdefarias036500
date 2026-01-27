package br.gov.seplag.musicapi.api.v1;

import br.gov.seplag.musicapi.api.v1.dto.AlbumRequest;
import br.gov.seplag.musicapi.api.v1.dto.AlbumResponse;
import br.gov.seplag.musicapi.service.AlbumService;
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
@RequestMapping("/v1/albuns")
public class AlbumController {
	private final AlbumService albumService;

	public AlbumController(AlbumService albumService) {
		this.albumService = albumService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AlbumResponse criar(@RequestBody AlbumRequest request) {
		return albumService.criar(request);
	}

	@PutMapping("/{id}")
	public AlbumResponse atualizar(@PathVariable Long id, @RequestBody AlbumRequest request) {
		return albumService.atualizar(id, request);
	}

	@GetMapping("/{id}")
	public AlbumResponse buscarPorId(@PathVariable Long id) {
		return albumService.buscarPorId(id);
	}

	@GetMapping
	public Page<AlbumResponse> listar(
		@RequestParam(name = "titulo", required = false) String titulo,
		@RequestParam(name = "artistaNome", required = false) String artistaNome,
		@RequestParam(name = "artistaId", required = false) Long artistaId,
		@RequestParam(name = "ordem", required = false) String ordem,
		@RequestParam(name = "pagina", defaultValue = "0") int pagina,
		@RequestParam(name = "tamanho", defaultValue = "20") int tamanho
	) {
		return albumService.listar(titulo, artistaNome, artistaId, ordem, pagina, tamanho);
	}
}

