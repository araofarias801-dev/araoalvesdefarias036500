package br.gov.seplag.musicapi.service;

import br.gov.seplag.musicapi.domain.CapaAlbum;
import br.gov.seplag.musicapi.repository.AlbumRepository;
import br.gov.seplag.musicapi.repository.CapaAlbumRepository;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CapaAlbumService {
	private final AlbumRepository albumRepository;
	private final CapaAlbumRepository capaAlbumRepository;
	private final MinioClient minioClient;
	private final String bucket;

	public CapaAlbumService(
		AlbumRepository albumRepository,
		CapaAlbumRepository capaAlbumRepository,
		MinioClient minioClient,
		@Value("${app.minio.bucket}") String bucket
	) {
		this.albumRepository = albumRepository;
		this.capaAlbumRepository = capaAlbumRepository;
		this.minioClient = minioClient;
		this.bucket = bucket;
	}

	@Transactional
	public void enviar(Long albumId, MultipartFile arquivo) {
		if (!albumRepository.existsById(albumId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "álbum não encontrado");
		}

		if (arquivo == null || arquivo.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "arquivo é obrigatório");
		}

		String contentType = Optional.ofNullable(arquivo.getContentType()).map(String::trim).orElse(null);
		String extensao = mapearExtensao(contentType);
		if (extensao == null) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "tipo de arquivo não suportado");
		}

		String objeto = "albuns/" + albumId + "/capa-" + UUID.randomUUID() + "." + extensao;

		try (InputStream inputStream = arquivo.getInputStream()) {
			garantirBucket();

			Optional<CapaAlbum> existenteOpt = capaAlbumRepository.findByAlbumId(albumId);
			if (existenteOpt.isPresent()) {
				CapaAlbum existente = existenteOpt.get();
				removerObjeto(existente.getBucket(), existente.getObjeto());
			}

			var resposta = minioClient.putObject(
				PutObjectArgs.builder()
					.bucket(bucket)
					.object(objeto)
					.stream(inputStream, arquivo.getSize(), -1)
					.contentType(contentType)
					.build()
			);

			CapaAlbum capa = existenteOpt.orElseGet(CapaAlbum::new);
			capa.setAlbumId(albumId);
			capa.setBucket(bucket);
			capa.setObjeto(objeto);
			capa.setContentType(contentType);
			capa.setTamanho(arquivo.getSize());
			capa.setEtag(resposta.etag());
			capa.setNomeOriginal(arquivo.getOriginalFilename());
			capaAlbumRepository.save(capa);
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "falha ao enviar capa do álbum", ex);
		}
	}

	@Transactional(readOnly = true)
	public String gerarUrlPorAlbumId(Long albumId) {
		if (!albumRepository.existsById(albumId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "álbum não encontrado");
		}

		CapaAlbum capa = capaAlbumRepository.findByAlbumId(albumId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "capa não encontrada"));

		try {
			return minioClient.getPresignedObjectUrl(
				GetPresignedObjectUrlArgs.builder()
					.method(Method.GET)
					.bucket(capa.getBucket())
					.object(capa.getObjeto())
					.expiry(30, TimeUnit.MINUTES)
					.build()
			);
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "falha ao gerar url da capa", ex);
		}
	}

	private void garantirBucket() throws Exception {
		boolean existe = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
		if (!existe) {
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
		}
	}

	private void removerObjeto(String bucket, String objeto) throws Exception {
		minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objeto).build());
	}

	private String mapearExtensao(String contentType) {
		if (contentType == null) {
			return null;
		}
		return switch (contentType) {
			case "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> null;
		};
	}
}
