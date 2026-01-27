package br.gov.seplag.musicapi.service;

import br.gov.seplag.musicapi.api.v1.dto.AlbumRequest;
import br.gov.seplag.musicapi.api.v1.dto.AlbumResponse;
import br.gov.seplag.musicapi.api.v1.dto.ArtistaResumoResponse;
import br.gov.seplag.musicapi.domain.Album;
import br.gov.seplag.musicapi.domain.Artista;
import br.gov.seplag.musicapi.repository.AlbumRepository;
import br.gov.seplag.musicapi.repository.ArtistaRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AlbumService {
	private final AlbumRepository albumRepository;
	private final ArtistaRepository artistaRepository;

	public AlbumService(AlbumRepository albumRepository, ArtistaRepository artistaRepository) {
		this.albumRepository = albumRepository;
		this.artistaRepository = artistaRepository;
	}

	@Transactional
	public AlbumResponse criar(AlbumRequest request) {
		String titulo = normalizarTitulo(request);
		if (titulo == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "titulo é obrigatório");
		}

		Album album = new Album();
		album.setTitulo(titulo);
		album = albumRepository.save(album);

		if (request != null && request.getArtistaIds() != null) {
			List<Artista> artistas = buscarArtistasValidos(request.getArtistaIds());
			for (Artista artista : artistas) {
				artista.getAlbuns().add(album);
			}
			artistaRepository.saveAll(artistas);
			album.getArtistas().clear();
			album.getArtistas().addAll(artistas);
		}

		return buscarPorId(album.getId());
	}

	@Transactional
	public AlbumResponse atualizar(Long id, AlbumRequest request) {
		Album album = albumRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "álbum não encontrado"));

		if (request != null && request.getTitulo() != null) {
			String titulo = normalizarTitulo(request);
			if (titulo == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "titulo é obrigatório");
			}
			album.setTitulo(titulo);
		}
		albumRepository.save(album);

		if (request != null && request.getArtistaIds() != null) {
			List<Artista> artistasAntigos = artistaRepository.buscarPorAlbumId(id);
			for (Artista artista : artistasAntigos) {
				artista.getAlbuns().removeIf(a -> id.equals(a.getId()));
			}
			artistaRepository.saveAll(artistasAntigos);

			List<Artista> novosArtistas = buscarArtistasValidos(request.getArtistaIds());
			for (Artista artista : novosArtistas) {
				artista.getAlbuns().add(album);
			}
			artistaRepository.saveAll(novosArtistas);
			album.getArtistas().clear();
			album.getArtistas().addAll(novosArtistas);
		}

		return buscarPorId(id);
	}

	@Transactional(readOnly = true)
	public AlbumResponse buscarPorId(Long id) {
		Album album = albumRepository.buscarComArtistasPorId(id);
		if (album == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "álbum não encontrado");
		}
		return toResponse(album);
	}

	@Transactional(readOnly = true)
	public Page<AlbumResponse> listar(
		String titulo,
		String artistaNome,
		Long artistaId,
		String ordem,
		int pagina,
		int tamanho
	) {
		Sort sort = Sort.by(parseDirection(ordem), "titulo");
		Pageable pageable = PageRequest.of(pagina, tamanho, sort);

		String tituloParam = normalizarFiltro(titulo);
		String artistaNomeParam = normalizarFiltro(artistaNome);

		return albumRepository.buscar(tituloParam, artistaNomeParam, artistaId, pageable).map(this::toResponse);
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

	private String normalizarTitulo(AlbumRequest request) {
		if (request == null || request.getTitulo() == null) {
			return null;
		}
		String titulo = request.getTitulo().trim();
		return titulo.isBlank() ? null : titulo;
	}

	private String normalizarFiltro(String valor) {
		if (valor == null) {
			return null;
		}
		String normalizado = valor.trim();
		return normalizado.isBlank() ? null : normalizado;
	}

	private List<Artista> buscarArtistasValidos(List<Long> artistaIds) {
		if (artistaIds.isEmpty()) {
			return List.of();
		}

		Set<Long> ids = new LinkedHashSet<>();
		for (Long id : artistaIds) {
			if (id == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "artistaIds não pode conter null");
			}
			ids.add(id);
		}

		List<Artista> artistas = artistaRepository.findAllById(ids);
		Set<Long> idsEncontrados = new LinkedHashSet<>();
		for (Artista artista : artistas) {
			idsEncontrados.add(artista.getId());
		}

		Set<Long> idsNaoEncontrados = new LinkedHashSet<>();
		for (Long id : ids) {
			if (!idsEncontrados.contains(id)) {
				idsNaoEncontrados.add(id);
			}
		}

		if (!idsNaoEncontrados.isEmpty()) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"artistas não encontrados: " + idsNaoEncontrados
			);
		}

		return artistas;
	}

	private AlbumResponse toResponse(Album album) {
		List<ArtistaResumoResponse> artistas = album.getArtistas().stream()
			.sorted(Comparator.comparing(Artista::getNome, Comparator.nullsLast(String::compareToIgnoreCase)))
			.map(a -> new ArtistaResumoResponse(a.getId(), a.getNome()))
			.toList();

		return new AlbumResponse(album.getId(), album.getTitulo(), artistas);
	}
}
