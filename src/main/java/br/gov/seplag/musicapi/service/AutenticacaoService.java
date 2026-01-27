package br.gov.seplag.musicapi.service;

import br.gov.seplag.musicapi.api.v1.dto.CadastroUsuarioRequest;
import br.gov.seplag.musicapi.api.v1.dto.LoginRequest;
import br.gov.seplag.musicapi.api.v1.dto.RenovarTokenRequest;
import br.gov.seplag.musicapi.api.v1.dto.TokenResponse;
import br.gov.seplag.musicapi.domain.RefreshToken;
import br.gov.seplag.musicapi.domain.Usuario;
import br.gov.seplag.musicapi.repository.RefreshTokenRepository;
import br.gov.seplag.musicapi.repository.UsuarioRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutenticacaoService {
	private final UsuarioRepository usuarioRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final SecureRandom secureRandom = new SecureRandom();

	private final String issuer;
	private final long accessTokenMinutes;
	private final long refreshTokenDays;

	public AutenticacaoService(
		UsuarioRepository usuarioRepository,
		RefreshTokenRepository refreshTokenRepository,
		PasswordEncoder passwordEncoder,
		JwtEncoder jwtEncoder,
		@Value("${app.jwt.issuer}") String issuer,
		@Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
		@Value("${app.jwt.refresh-token-days}") long refreshTokenDays
	) {
		this.usuarioRepository = usuarioRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.accessTokenMinutes = accessTokenMinutes;
		this.refreshTokenDays = refreshTokenDays;
	}

	@Transactional
	public void cadastrar(CadastroUsuarioRequest request) {
		String username = normalizarObrigatorio(request == null ? null : request.getUsername(), "username é obrigatório");
		String senha = normalizarObrigatorio(request == null ? null : request.getSenha(), "senha é obrigatória");

		Optional<Usuario> existente = usuarioRepository.findByUsername(username);
		if (existente.isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "username já existe");
		}

		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setSenhaHash(passwordEncoder.encode(senha));
		usuario.setRoles("ROLE_USER");
		usuarioRepository.save(usuario);
	}

	@Transactional
	public TokenResponse login(LoginRequest request) {
		String username = normalizarObrigatorio(request == null ? null : request.getUsername(), "username é obrigatório");
		String senha = normalizarObrigatorio(request == null ? null : request.getSenha(), "senha é obrigatória");

		Usuario usuario = usuarioRepository.findByUsername(username)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciais inválidas"));

		if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciais inválidas");
		}

		return emitirTokens(usuario);
	}

	@Transactional
	public TokenResponse renovar(RenovarTokenRequest request) {
		String token = normalizarObrigatorio(request == null ? null : request.getRefreshToken(), "refreshToken é obrigatório");

		RefreshToken refresh = refreshTokenRepository.findByToken(token)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token inválido"));

		if (refresh.isRevogado()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token inválido");
		}

		if (refresh.getExpiraEm().isBefore(Instant.now())) {
			refresh.setRevogado(true);
			refreshTokenRepository.save(refresh);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token expirado");
		}

		refresh.setRevogado(true);
		refreshTokenRepository.save(refresh);

		return emitirTokens(refresh.getUsuario());
	}

	private TokenResponse emitirTokens(Usuario usuario) {
		Instant agora = Instant.now();
		Instant expiraEm = agora.plus(Duration.ofMinutes(accessTokenMinutes));

		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(issuer)
			.issuedAt(agora)
			.expiresAt(expiraEm)
			.subject(usuario.getUsername())
			.claim("roles", usuario.getRoles())
			.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

		String refreshToken = gerarRefreshToken();
		Instant refreshExpiraEm = agora.plus(Duration.ofDays(refreshTokenDays));

		RefreshToken refresh = new RefreshToken();
		refresh.setUsuario(usuario);
		refresh.setToken(refreshToken);
		refresh.setExpiraEm(refreshExpiraEm);
		refresh.setRevogado(false);
		refreshTokenRepository.save(refresh);

		long expiresInSeconds = Duration.between(agora, expiraEm).toSeconds();
		return new TokenResponse(accessToken, refreshToken, "Bearer", expiresInSeconds);
	}

	private String gerarRefreshToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String normalizarObrigatorio(String valor, String mensagemErro) {
		if (valor == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemErro);
		}
		String normalizado = valor.trim();
		if (normalizado.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagemErro);
		}
		return normalizado;
	}
}
