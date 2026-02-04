package br.gov.seplag.musicapi.api.v1;

import br.gov.seplag.musicapi.api.v1.dto.AlbumRequest;
import br.gov.seplag.musicapi.api.v1.dto.AlbumResponse;
import br.gov.seplag.musicapi.api.v1.dto.CapaUrlResponse;
import br.gov.seplag.musicapi.api.v1.dto.CapaUrlsResponse;
import br.gov.seplag.musicapi.service.AlbumService;
import br.gov.seplag.musicapi.service.CapaAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/albuns")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Álbuns", description = "Operações de cadastro, consulta e upload de capas de álbuns.")
public class AlbumController {
	private final AlbumService albumService;
	private final CapaAlbumService capaAlbumService;

	public AlbumController(AlbumService albumService, CapaAlbumService capaAlbumService) {
		this.albumService = albumService;
		this.capaAlbumService = capaAlbumService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Criar álbum", description = "Cria um novo álbum e associa artistas (N:N).")
	public AlbumResponse criar(@RequestBody AlbumRequest request) {
		return albumService.criar(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualizar álbum", description = "Atualiza os dados de um álbum pelo id.")
	public AlbumResponse atualizar(@PathVariable Long id, @RequestBody AlbumRequest request) {
		return albumService.atualizar(id, request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Buscar álbum por id", description = "Retorna um álbum pelo id.")
	public AlbumResponse buscarPorId(@PathVariable Long id) {
		return albumService.buscarPorId(id);
	}

	@GetMapping
	@Operation(
		summary = "Listar álbuns",
		description = "Lista álbuns com paginação, filtros opcionais e ordenação asc/desc."
	)
	public Page<AlbumResponse> listar(
		@RequestParam(name = "titulo", required = false) String titulo,
		@RequestParam(name = "artistaNome", required = false) String artistaNome,
		@RequestParam(name = "artistaId", required = false) Long artistaId,
		@RequestParam(name = "temCantor", required = false) Boolean temCantor,
		@RequestParam(name = "temBanda", required = false) Boolean temBanda,
		@RequestParam(name = "ordem", required = false) String ordem,
		@RequestParam(name = "pagina", defaultValue = "0") int pagina,
		@RequestParam(name = "tamanho", defaultValue = "20") int tamanho
	) {
		return albumService.listar(titulo, artistaNome, artistaId, temCantor, temBanda, ordem, pagina, tamanho);
	}

	@PostMapping(value = "/{id}/capa", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Enviar capa do álbum", description = "Faz upload de uma ou mais imagens de capa do álbum (MinIO).")
	public void enviarCapa(
		@PathVariable Long id,
		@RequestPart(value = "arquivo", required = false) MultipartFile arquivo,
		@RequestPart(value = "arquivos", required = false) MultipartFile[] arquivos
	) {
		if (arquivos != null && arquivos.length > 0) {
			capaAlbumService.enviar(id, arquivos);
			return;
		}
		capaAlbumService.enviar(id, arquivo);
	}

	@GetMapping("/{id}/capa/url")
	@Operation(
		summary = "Obter URL pré-assinada da capa",
		description = "Gera um link pré-assinado com expiração para baixar a última capa enviada do álbum."
	)
	public CapaUrlResponse obterUrlCapa(@PathVariable Long id) {
		return new CapaUrlResponse(capaAlbumService.gerarUrlPorAlbumId(id));
	}

	@GetMapping("/{id}/capa/urls")
	@Operation(
		summary = "Obter URLs pré-assinadas das capas",
		description = "Gera links pré-assinados com expiração para baixar as capas do álbum."
	)
	public CapaUrlsResponse obterUrlsCapas(@PathVariable Long id) {
		return new CapaUrlsResponse(capaAlbumService.gerarUrlsPorAlbumId(id));
	}
}

