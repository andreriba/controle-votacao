package com.controlevotacao.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class PautaVotacao {

    @EmbeddedId
    private PautaVotacaoId id;  // chave composta

    private String voto;

    public PautaVotacao() {}

    public PautaVotacao(PautaVotacaoId id, String voto) {
        this.id = id;
        this.voto = voto;
    }

    public PautaVotacaoId getId() {
        return id;
    }

    public void setId(PautaVotacaoId id) {
        this.id = id;
    }

    public String getVoto() {
        return voto;
    }

    public void setVoto(String voto) {
        this.voto = voto;
    }
}
