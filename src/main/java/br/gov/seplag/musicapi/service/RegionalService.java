package br.gov.seplag.musicapi.service;

import br.gov.seplag.musicapi.api.v1.dto.RegionalResponse;
import br.gov.seplag.musicapi.api.v1.dto.SincronizarRegionaisResponse;
import br.gov.seplag.musicapi.domain.Regional;
import br.gov.seplag.musicapi.repository.RegionalRepository;
import br.gov.seplag.musicapi.service.RegionaisIntegradorClient.RegionalIntegradorResponse;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegionalService {
	private static final Logger logger = LoggerFactory.getLogger(RegionalService.class);

	private final RegionalRepository regionalRepository;
	private final RegionaisIntegradorClient regionaisIntegradorClient;

	@Value("${app.regionais.sync.enabled:false}")
	private boolean sincronizacaoAgendadaHabilitada;

	public RegionalService(RegionalRepository regionalRepository, RegionaisIntegradorClient regionaisIntegradorClient) {
		this.regionalRepository = regionalRepository;
		this.regionaisIntegradorClient = regionaisIntegradorClient;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void sincronizarAoIniciar() {
		executarSincronizacao("inicialização");
	}

	@Scheduled(cron = "${app.regionais.sync.cron:0 */30 * * * *}")
	public void sincronizarAgendado() {
		executarSincronizacao("agendada");
	}

	private void executarSincronizacao(String origem) {
		if (!sincronizacaoAgendadaHabilitada) {
			return;
		}
		try {
			SincronizarRegionaisResponse response = sincronizar();
			logger.info(
				"Sincronização {} de regionais concluída: inseridos={}, inativados={}",
				origem,
				response.getInseridos(),
				response.getInativados()
			);
		} catch (Exception ex) {
			logger.warn("Falha na sincronização {} de regionais", origem, ex);
		}
	}

	@Transactional(readOnly = true)
	public List<RegionalResponse> listar(Boolean ativo, String nome) {
		boolean filtroAtivo = Optional.ofNullable(ativo).orElse(true);
		List<Regional> regionais;

		if (nome == null || nome.isBlank()) {
			regionais = filtroAtivo ? regionalRepository.findByAtivoTrue() : regionalRepository.findByAtivoFalse();
		} else {
			regionais = regionalRepository.findByAtivoAndNomeContainingIgnoreCase(filtroAtivo, nome.trim());
		}

		return regionais.stream()
			.sorted(Comparator.comparing(Regional::getNome, String.CASE_INSENSITIVE_ORDER))
			.map(this::toResponse)
			.toList();
	}

	@Transactional
	public SincronizarRegionaisResponse sincronizar() {
		List<RegionalIntegradorResponse> integrador;
		try {
			integrador = Optional.ofNullable(regionaisIntegradorClient.listarRegionais()).orElse(List.of());
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "falha ao consultar integrador de regionais", ex);
		}

		Set<Integer> idsNoEndpoint = new HashSet<>();

		int inseridos = 0;
		int inativados = 0;

		for (RegionalIntegradorResponse item : integrador) {
			if (item == null || item.getId() == null || item.getNome() == null) {
				continue;
			}

			Integer idIntegrador = item.getId();
			String nome = item.getNome().trim();
			idsNoEndpoint.add(idIntegrador);

			Optional<Regional> ativoAtual = regionalRepository.findFirstByIdIntegradorAndAtivoTrue(idIntegrador);
			if (ativoAtual.isEmpty()) {
				regionalRepository.save(novaRegionalAtiva(idIntegrador, nome));
				inseridos++;
				continue;
			}

			Regional existente = ativoAtual.get();
			if (!nome.equals(existente.getNome())) {
				existente.setAtivo(false);
				regionalRepository.save(existente);
				inativados++;

				regionalRepository.save(novaRegionalAtiva(idIntegrador, nome));
				inseridos++;
			}
		}

		if (idsNoEndpoint.isEmpty()) {
			List<Regional> ativos = regionalRepository.findByAtivoTrue();
			for (Regional r : ativos) {
				r.setAtivo(false);
			}
			inativados += ativos.size();
			regionalRepository.saveAll(ativos);
		} else {
			List<Regional> paraInativar = regionalRepository.findByAtivoTrueAndIdIntegradorNotIn(idsNoEndpoint);
			for (Regional r : paraInativar) {
				r.setAtivo(false);
			}
			inativados += paraInativar.size();
			regionalRepository.saveAll(paraInativar);
		}

		return new SincronizarRegionaisResponse(inseridos, inativados);
	}

	private Regional novaRegionalAtiva(Integer idIntegrador, String nome) {
		Regional regional = new Regional();
		regional.setIdIntegrador(idIntegrador);
		regional.setNome(nome);
		regional.setAtivo(true);
		return regional;
	}

	private RegionalResponse toResponse(Regional regional) {
		return new RegionalResponse(regional.getId(), regional.getIdIntegrador(), regional.getNome(), regional.isAtivo());
	}
}
