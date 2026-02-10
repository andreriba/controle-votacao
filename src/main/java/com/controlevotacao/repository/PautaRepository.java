package com.controlevotacao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.controlevotacao.model.Pauta;

public interface PautaRepository extends JpaRepository<Pauta, String> {
	
	Optional<Pauta> findByStatusPauta(String statusPauta);
	
	List<Pauta> findAllByStatusPauta(String statusPauta);
}
