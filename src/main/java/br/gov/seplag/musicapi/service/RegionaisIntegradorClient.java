package br.gov.seplag.musicapi.service;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import br.gov.seplag.musicapi.config.IntegradorFeignConfig;

@FeignClient(
	name = "integrador-regionais",
	url = "${app.integrador.regionais-url:https://integrador-argus-api.geia.vip/v1/regionais}",
	configuration = IntegradorFeignConfig.class
)
public interface RegionaisIntegradorClient {
	@GetMapping
	List<RegionalIntegradorResponse> listarRegionais();

	class RegionalIntegradorResponse {
		private Integer id;
		private String nome;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}
	}
}
