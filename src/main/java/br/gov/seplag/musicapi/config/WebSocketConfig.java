package br.gov.seplag.musicapi.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private final String allowedOrigins;

	public WebSocketConfig(@Value("${app.cors.allowed-origins:}") String allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		List<String> origins = parseAllowedOrigins(allowedOrigins);
		if (origins.isEmpty()) {
			origins = List.of("http://localhost:5500", "http://127.0.0.1:5500");
		}
		String[] originsArray = origins.toArray(String[]::new);

		var sockJsEndpoint = registry.addEndpoint("/ws");
		sockJsEndpoint.setAllowedOrigins(originsArray);
		sockJsEndpoint.withSockJS();

		var nativeEndpoint = registry.addEndpoint("/ws-native");
		nativeEndpoint.setAllowedOrigins(originsArray);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");
		registry.setApplicationDestinationPrefixes("/app");
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
