package br.gov.seplag.musicapi.api.v1.dto;

public class RenovarTokenRequest {
	private String refreshToken;

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
