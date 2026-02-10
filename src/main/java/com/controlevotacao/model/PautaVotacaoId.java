package com.controlevotacao.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class PautaVotacaoId implements Serializable {

    private String idPauta;
    private String codCpf;

    public PautaVotacaoId() {}

    public PautaVotacaoId(String idPauta, String codCpf) {
        this.idPauta = idPauta;
        this.codCpf = codCpf;
    }

    public String getIdPauta() {
        return idPauta;
    }

    public void setIdPauta(String idPauta) {
        this.idPauta = idPauta;
    }

    public String getCodCpf() {
        return codCpf;
    }

    public void setCodCpf(String codCpf) {
        this.codCpf = codCpf;
    }

    // equals e hashCode são obrigatórios para chave composta
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PautaVotacaoId)) return false;
        PautaVotacaoId that = (PautaVotacaoId) o;
        return idPauta.equals(that.idPauta) && codCpf.equals(that.codCpf);
    }

    @Override
    public int hashCode() {
        return idPauta.hashCode() + codCpf.hashCode();
    }
}
