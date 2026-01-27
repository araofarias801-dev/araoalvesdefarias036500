package br.gov.seplag.musicapi.api.v1.dto;

public class TokenResponse {
	private String accessToken;
	private String refreshToken;
	private String tokenType;
	private long expiresInSeconds;

	public TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.tokenType = tokenType;
		this.expiresInSeconds = expiresInSeconds;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public long getExpiresInSeconds() {
		return expiresInSeconds;
	}
}
