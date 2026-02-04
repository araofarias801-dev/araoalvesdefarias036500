package br.gov.seplag.musicapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@OpenAPIDefinition(
	info = @Info(
		title = "Music API",
		description = "API REST para cadastro e consulta de artistas e álbuns.",
		version = "v1"
	)
)
public class MusicApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(MusicApiApplication.class, args);
	}
}

