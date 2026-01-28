package br.gov.seplag.musicapi.api.v1;

import br.gov.seplag.musicapi.api.v1.dto.CadastroUsuarioRequest;
import br.gov.seplag.musicapi.api.v1.dto.LoginRequest;
import br.gov.seplag.musicapi.api.v1.dto.RenovarTokenRequest;
import br.gov.seplag.musicapi.api.v1.dto.TokenResponse;
import br.gov.seplag.musicapi.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/autenticacao")
@Tag(name = "Autenticação", description = "Operações de cadastro, login e renovação de token JWT.")
public class AutenticacaoController {
	private final AutenticacaoService autenticacaoService;

	public AutenticacaoController(AutenticacaoService autenticacaoService) {
		this.autenticacaoService = autenticacaoService;
	}

	@PostMapping("/cadastrar")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Cadastrar usuário", description = "Cria um usuário para autenticação no sistema.")
	public void cadastrar(@RequestBody CadastroUsuarioRequest request) {
		autenticacaoService.cadastrar(request);
	}

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Gera access token e refresh token para o usuário.")
	public TokenResponse login(@RequestBody LoginRequest request) {
		return autenticacaoService.login(request);
	}

	@PostMapping("/renovar")
	@Operation(summary = "Renovar token", description = "Renova o access token usando o refresh token.")
	public TokenResponse renovar(@RequestBody RenovarTokenRequest request) {
		return autenticacaoService.renovar(request);
	}
}
