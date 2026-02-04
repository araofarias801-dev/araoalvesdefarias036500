package br.gov.seplag.musicapi.service;

import br.gov.seplag.musicapi.api.v1.dto.ArtistaRequest;
import br.gov.seplag.musicapi.api.v1.dto.ArtistaResponse;
import br.gov.seplag.musicapi.domain.Artista;
import br.gov.seplag.musicapi.domain.ArtistaTipo;
import br.gov.seplag.musicapi.repository.ArtistaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArtistaService {
	private final ArtistaRepository artistaRepository;

	public ArtistaService(ArtistaRepository artistaRepository) {
		this.artistaRepository = artistaRepository;
	}

	@Transactional
	public ArtistaResponse criar(ArtistaRequest request) {
		Artista artista = new Artista();
		artista.setNome(normalizarNome(request));
		artista.setTipo(normalizarTipoParaCriacao(request));
		artista = artistaRepository.save(artista);
		return toResponse(artista);
	}

	@Transactional
	public ArtistaResponse atualizar(Long id, ArtistaRequest request) {
		Artista artista = artistaRepository.findById(id).orElseThrow();
		artista.setNome(normalizarNome(request));
		if (request != null && request.getTipo() != null) {
			artista.setTipo(request.getTipo());
		}
		artista = artistaRepository.save(artista);
		return toResponse(artista);
	}

	@Transactional(readOnly = true)
	public ArtistaResponse buscarPorId(Long id) {
		Artista artista = artistaRepository.findById(id).orElseThrow();
		return toResponse(artista);
	}

	@Transactional(readOnly = true)
	public Page<ArtistaResponse> listar(String nome, String ordem, int pagina, int tamanho) {
		Sort sort = Sort.by(parseDirection(ordem), "nome");
		Pageable pageable = PageRequest.of(pagina, tamanho, sort);

		if (nome == null || nome.isBlank()) {
			return artistaRepository.findAll(pageable).map(this::toResponse);
		}
		return artistaRepository.findByNomeContainingIgnoreCase(nome.trim(), pageable).map(this::toResponse);
	}

	private Direction parseDirection(String ordem) {
		return Optional.ofNullable(ordem)
			.map(String::trim)
			.map(String::toUpperCase)
			.flatMap(value -> {
				try {
					return Optional.of(Direction.valueOf(value));
				} catch (IllegalArgumentException ex) {
					return Optional.empty();
				}
			})
			.orElse(Direction.ASC);
	}

	private String normalizarNome(ArtistaRequest request) {
		if (request == null || request.getNome() == null) {
			return null;
		}
		return request.getNome().trim();
	}

	private ArtistaTipo normalizarTipoParaCriacao(ArtistaRequest request) {
		if (request == null || request.getTipo() == null) {
			return ArtistaTipo.CANTOR;
		}
		return request.getTipo();
	}

	private ArtistaResponse toResponse(Artista artista) {
		return new ArtistaResponse(artista.getId(), artista.getNome(), artista.getTipo());
	}
}
