package br.gov.seplag.musicapi.api.v1.dto;

import java.util.List;

public class CapaUrlsResponse {
	private List<String> urls;

	public CapaUrlsResponse(List<String> urls) {
		this.urls = urls;
	}

	public List<String> getUrls() {
		return urls;
	}
}
