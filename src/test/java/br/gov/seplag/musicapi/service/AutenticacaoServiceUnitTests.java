package br.gov.seplag.musicapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.seplag.musicapi.api.v1.dto.CadastroUsuarioRequest;
import br.gov.seplag.musicapi.api.v1.dto.LoginRequest;
import br.gov.seplag.musicapi.api.v1.dto.RenovarTokenRequest;
import br.gov.seplag.musicapi.api.v1.dto.TokenResponse;
import br.gov.seplag.musicapi.domain.RefreshToken;
import br.gov.seplag.musicapi.domain.Usuario;
import br.gov.seplag.musicapi.repository.RefreshTokenRepository;
import br.gov.seplag.musicapi.repository.UsuarioRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceUnitTests {
	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtEncoder jwtEncoder;

	private AutenticacaoService autenticacaoService;

	@BeforeEach
	void setup() {
		autenticacaoService = new AutenticacaoService(
			usuarioRepository,
			refreshTokenRepository,
			passwordEncoder,
			jwtEncoder,
			"issuer-test",
			5,
			7
		);
	}

	@Test
	void cadastrarQuandoUsernameNuloRetorna400() {
		assertThatThrownBy(() -> autenticacaoService.cadastrar(null))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				assertThat(ex.getReason()).isEqualTo("username é obrigatório");
			});
	}

	@Test
	void cadastrarQuandoUsernameExisteRetorna409() {
		CadastroUsuarioRequest request = new CadastroUsuarioRequest();
		request.setUsername("user");
		request.setSenha("senha");
		when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(new Usuario()));

		assertThatThrownBy(() -> autenticacaoService.cadastrar(request))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
				assertThat(ex.getReason()).isEqualTo("username já existe");
			});

		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void cadastrarSalvaUsuarioComSenhaHashERole() {
		CadastroUsuarioRequest request = new CadastroUsuarioRequest();
		request.setUsername("  user  ");
		request.setSenha("  senha  ");
		when(usuarioRepository.findByUsername("user")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("senha")).thenReturn("hash");

		autenticacaoService.cadastrar(request);

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(captor.capture());
		assertThat(captor.getValue().getUsername()).isEqualTo("user");
		assertThat(captor.getValue().getSenhaHash()).isEqualTo("hash");
		assertThat(captor.getValue().getRoles()).isEqualTo("ROLE_USER");
	}

	@Test
	void loginComCredenciaisInvalidasRetorna401() {
		LoginRequest request = new LoginRequest();
		request.setUsername("user");
		request.setSenha("senha");

		when(usuarioRepository.findByUsername("user")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> autenticacaoService.login(request))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
				assertThat(ex.getReason()).isEqualTo("credenciais inválidas");
			});
	}

	@Test
	void loginQuandoSenhaNaoConfereRetorna401() {
		LoginRequest request = new LoginRequest();
		request.setUsername("user");
		request.setSenha("senha");

		Usuario usuario = new Usuario();
		usuario.setUsername("user");
		usuario.setSenhaHash("hash");
		usuario.setRoles("ROLE_USER");

		when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("senha", "hash")).thenReturn(false);

		assertThatThrownBy(() -> autenticacaoService.login(request))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
				assertThat(ex.getReason()).isEqualTo("credenciais inválidas");
			});

		verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
	}

	@Test
	void loginEmiteTokensERegistraRefresh() {
		LoginRequest request = new LoginRequest();
		request.setUsername("user");
		request.setSenha("senha");

		Usuario usuario = new Usuario();
		usuario.setUsername("user");
		usuario.setSenhaHash("hash");
		usuario.setRoles("ROLE_USER");

		when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("senha", "hash")).thenReturn(true);

		Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
		when(jwt.getTokenValue()).thenReturn("access");
		when(jwtEncoder.encode(any())).thenReturn(jwt);

		when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TokenResponse response = autenticacaoService.login(request);

		assertThat(response.getAccessToken()).isEqualTo("access");
		assertThat(response.getTokenType()).isEqualTo("Bearer");
		assertThat(response.getExpiresInSeconds()).isEqualTo(300L);
		assertThat(response.getRefreshToken()).isNotBlank();

		ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(refreshTokenRepository).save(captor.capture());
		assertThat(captor.getValue().getUsuario()).isSameAs(usuario);
		assertThat(captor.getValue().getToken()).isNotBlank();
		assertThat(captor.getValue().isRevogado()).isFalse();
		assertThat(captor.getValue().getExpiraEm()).isAfter(Instant.now());
	}

	@Test
	void renovarQuandoRefreshExpiradoRevogaESinaliza401() {
		RenovarTokenRequest request = new RenovarTokenRequest();
		request.setRefreshToken("rt");

		RefreshToken refresh = new RefreshToken();
		refresh.setToken("rt");
		refresh.setRevogado(false);
		refresh.setExpiraEm(Instant.now().minusSeconds(1));
		refresh.setUsuario(usuario("user"));

		when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(refresh));
		when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThatThrownBy(() -> autenticacaoService.renovar(request))
			.isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
				assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
				assertThat(ex.getReason()).isEqualTo("refresh token expirado");
			});

		assertThat(refresh.isRevogado()).isTrue();
		verify(refreshTokenRepository).save(eq(refresh));
	}

	@Test
	void renovarQuandoValidoRevogaAnteriorEEmiteNovosTokens() {
		RenovarTokenRequest request = new RenovarTokenRequest();
		request.setRefreshToken("rt");

		Usuario usuario = usuario("user");
		usuario.setRoles("ROLE_USER");

		RefreshToken refresh = new RefreshToken();
		refresh.setToken("rt");
		refresh.setRevogado(false);
		refresh.setExpiraEm(Instant.now().plusSeconds(60));
		refresh.setUsuario(usuario);

		when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(refresh));

		Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
		when(jwt.getTokenValue()).thenReturn("access2");
		when(jwtEncoder.encode(any())).thenReturn(jwt);

		when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TokenResponse response = autenticacaoService.renovar(request);

		assertThat(refresh.isRevogado()).isTrue();
		assertThat(response.getAccessToken()).isEqualTo("access2");
		assertThat(response.getRefreshToken()).isNotBlank();
		verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
	}

	private static Usuario usuario(String username) {
		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setSenhaHash("hash");
		usuario.setRoles("ROLE_USER");
		return usuario;
	}
}
