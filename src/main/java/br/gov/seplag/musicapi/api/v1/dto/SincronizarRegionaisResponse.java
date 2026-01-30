package br.gov.seplag.musicapi.api.v1.dto;

public class SincronizarRegionaisResponse {
	private int inseridos;
	private int inativados;

	public SincronizarRegionaisResponse(int inseridos, int inativados) {
		this.inseridos = inseridos;
		this.inativados = inativados;
	}

	public int getInseridos() {
		return inseridos;
	}

	public int getInativados() {
		return inativados;
	}
}
