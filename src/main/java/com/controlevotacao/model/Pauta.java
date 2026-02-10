package com.controlevotacao.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Pauta {

    @Id
    private String idPauta;
    private String statusPauta;
    private LocalDateTime inicioSessao;
    private LocalDateTime fimSessao;

    public String getIdPauta() {
        return idPauta;
    }

    public void setIdPauta(String idPauta) {
        this.idPauta = idPauta;
    }

    public String getStatusPauta() {
        return statusPauta;
    }

    public void setStatusPauta(String statusPauta) {
        this.statusPauta = statusPauta;
    }

	public LocalDateTime getInicioSessao() {
		return inicioSessao;
	}

	public void setInicioSessao(LocalDateTime inicioSessao) {
		this.inicioSessao = inicioSessao;
	}

	public LocalDateTime getFimSessao() {
		return fimSessao;
	}

	public void setFimSessao(LocalDateTime fimSessao) {
		this.fimSessao = fimSessao;
	}  
}
