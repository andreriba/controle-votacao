package com.controlevotacao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.controlevotacao.model.PautaVotacao;
import com.controlevotacao.model.PautaVotacaoId;

public interface PautaVotacaoRepository extends JpaRepository<PautaVotacao, PautaVotacaoId> {
    List<PautaVotacao> findByIdIdPauta(String idPauta);

    Optional<PautaVotacao> findByIdIdPautaAndIdCodCpf(String idPauta, String codCpf);
    
    long countByIdIdPautaAndVoto(String idPauta, String voto);
}
