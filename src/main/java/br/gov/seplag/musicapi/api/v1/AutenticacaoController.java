package br.gov.seplag.musicapi.api.v1;

import br.gov.seplag.musicapi.api.v1.dto.CadastroUsuarioRequest;
import br.gov.seplag.musicapi.api.v1.dto.LoginRequest;
import br.gov.seplag.musicapi.api.v1.dto.RenovarTokenRequest;
import br.gov.seplag.musicapi.api.v1.dto.TokenResponse;
import br.gov.seplag.musicapi.service.AutenticacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/autenticacao")
public class AutenticacaoController {
	private final AutenticacaoService autenticacaoService;

	public AutenticacaoController(AutenticacaoService autenticacaoService) {
		this.autenticacaoService = autenticacaoService;
	}

	@PostMapping("/cadastrar")
	@ResponseStatus(HttpStatus.CREATED)
	public void cadastrar(@RequestBody CadastroUsuarioRequest request) {
		autenticacaoService.cadastrar(request);
	}

	@PostMapping("/login")
	public TokenResponse login(@RequestBody LoginRequest request) {
		return autenticacaoService.login(request);
	}

	@PostMapping("/renovar")
	public TokenResponse renovar(@RequestBody RenovarTokenRequest request) {
		return autenticacaoService.renovar(request);
	}
}
