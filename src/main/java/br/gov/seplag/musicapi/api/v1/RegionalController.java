package br.gov.seplag.musicapi.api.v1;

import br.gov.seplag.musicapi.api.v1.dto.RegionalResponse;
import br.gov.seplag.musicapi.api.v1.dto.SincronizarRegionaisResponse;
import br.gov.seplag.musicapi.service.RegionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/regionais")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Regionais", description = "Operações de sincronização e consulta de regionais.")
public class RegionalController {
	private final RegionalService regionalService;

	public RegionalController(RegionalService regionalService) {
		this.regionalService = regionalService;
	}

	@GetMapping
	@Operation(summary = "Listar regionais", description = "Lista regionais ativas (padrão) ou inativas, com filtro por nome.")
	public List<RegionalResponse> listar(
		@RequestParam(name = "ativo", required = false) Boolean ativo,
		@RequestParam(name = "nome", required = false) String nome
	) {
		return regionalService.listar(ativo, nome);
	}

	@PostMapping("/sincronizar")
	@Operation(
		summary = "Sincronizar regionais",
		description = "Importa do endpoint externo, inativa ausentes e versiona alterações."
	)
	public SincronizarRegionaisResponse sincronizar() {
		return regionalService.sincronizar();
	}
}
