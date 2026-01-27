package br.gov.seplag.musicapi.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class SegurancaConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		RateLimitFilter rateLimitFilter
	) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.cors(Customizer.withDefaults());

		http.authorizeHttpRequests(auth -> auth
			.requestMatchers("/actuator/**").permitAll()
			.requestMatchers("/v1/ping").permitAll()
			.requestMatchers("/v1/autenticacao/**").permitAll()
			.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
			.requestMatchers("/v1/**").authenticated()
			.anyRequest().permitAll()
		);

		http.oauth2ResourceServer(oauth2 -> oauth2
			.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
		);

		http.addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public RateLimitFilter rateLimitFilter(
		@Value("${app.ratelimit.enabled}") boolean enabled,
		@Value("${app.ratelimit.requests-per-minute}") int requestsPerMinute
	) {
		return new RateLimitFilter(enabled, requestsPerMinute);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
		@Value("${app.cors.allowed-origins:}") String allowedOrigins
	) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(parseAllowedOrigins(allowedOrigins));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secret) {
		byte[] bytes = secretBytes(secret);
		return new NimbusJwtEncoder(new ImmutableSecret<>(bytes));
	}

	@Bean
	public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
		SecretKey key = secretKey(secret);
		return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
	}

	private byte[] secretBytes(String secret) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("app.jwt.secret é obrigatório");
		}
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("app.jwt.secret deve ter pelo menos 32 caracteres");
		}
		return bytes;
	}

	private SecretKey secretKey(String secret) {
		byte[] bytes = secretBytes(secret);
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
		defaultConverter.setAuthorityPrefix("");
		defaultConverter.setAuthoritiesClaimName("roles");

		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Object rolesClaim = jwt.getClaim("roles");
			if (rolesClaim instanceof String rolesString) {
				Collection<GrantedAuthority> authorities = new ArrayList<>();
				for (String role : rolesString.split(",")) {
					String trimmed = role.trim();
					if (!trimmed.isBlank()) {
						authorities.add(new SimpleGrantedAuthority(trimmed));
					}
				}
				return authorities;
			}
			return defaultConverter.convert(jwt);
		});
		return converter;
	}

	private List<String> parseAllowedOrigins(String allowedOrigins) {
		if (allowedOrigins == null) {
			return List.of();
		}
		String trimmed = allowedOrigins.trim();
		if (trimmed.isBlank()) {
			return List.of();
		}
		String[] parts = trimmed.split(",");
		List<String> result = new ArrayList<>();
		for (String part : parts) {
			String value = part.trim();
			if (!value.isBlank()) {
				result.add(value);
			}
		}
		return result;
	}
}
