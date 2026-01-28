package br.gov.seplag.musicapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {
	private final boolean enabled;
	private final int requestsPerMinute;
	private final Map<String, Deque<Long>> requests = new ConcurrentHashMap<>();

	public RateLimitFilter(boolean enabled, int requestsPerMinute) {
		this.enabled = enabled;
		this.requestsPerMinute = requestsPerMinute;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!enabled) {
			return true;
		}
		String path = request.getRequestURI();
		return path != null && (
			path.startsWith("/h2-console")
				|| path.startsWith("/actuator")
				|| path.equals("/v1/ping")
				|| path.startsWith("/v1/autenticacao")
		);
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String key = identificarChave(request);
		long agora = Instant.now().toEpochMilli();
		long janelaMs = 60_000L;

		Deque<Long> timestamps = requests.computeIfAbsent(key, k -> new ArrayDeque<>());
		boolean permitido;
		long retryAfterSeconds = 0;

		synchronized (timestamps) {
			while (!timestamps.isEmpty() && (agora - timestamps.peekFirst()) >= janelaMs) {
				timestamps.removeFirst();
			}

			if (timestamps.size() < requestsPerMinute) {
				timestamps.addLast(agora);
				permitido = true;
			} else {
				Long maisAntigo = timestamps.peekFirst();
				long faltaMs = maisAntigo == null ? janelaMs : Math.max(0, janelaMs - (agora - maisAntigo));
				retryAfterSeconds = Math.max(1, (long) Math.ceil(faltaMs / 1000.0));
				permitido = false;
			}
		}

		if (!permitido) {
			response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
			response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String identificarChave(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
			String name = authentication.getName();
			if (!"anonymousUser".equals(name)) {
				return "u:" + name;
			}
		}
		return "ip:" + request.getRemoteAddr();
	}
}
