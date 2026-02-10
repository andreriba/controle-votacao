package com.controlevotacao.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.controlevotacao.model.Pauta;
import com.controlevotacao.model.PautaVotacao;
import com.controlevotacao.model.PautaVotacaoId;
import com.controlevotacao.repository.PautaRepository;
import com.controlevotacao.repository.PautaVotacaoRepository;

@Service
public class VotacaoService {

	private final PautaRepository pautaRepository;
	private final PautaVotacaoRepository pautaVotacaoRepository;
	private final TaskScheduler scheduler;
	private final RestTemplate restTemplate = new RestTemplate();
	Logger log = LoggerFactory.getLogger(VotacaoService.class);
	
	//obtem o serviço externo de validação do cpf
	@Value("${app.url.valida.cpf}")
    private String urlValidaCpf;
	
	// injeta a propriedade do application.properties
	// se não existir, assume 1 minuto como default
	@Value("${sessao.votacao.duracao:1}")
	private int duracaoDefault;

	public VotacaoService(PautaRepository pautaRepository,
			              PautaVotacaoRepository pautaVotacaoRepository,
			              TaskScheduler scheduler) {
		this.pautaRepository = pautaRepository;
		this.pautaVotacaoRepository = pautaVotacaoRepository;
		this.scheduler = scheduler;
	}

	public String criarPauta(String idPauta) {
	    try {
	        Optional<Pauta> existente = pautaRepository.findById(idPauta);
	        if (existente.isPresent()) {
	            return "EXISTENTE";
	        }

	        Pauta pauta = new Pauta();
	        pauta.setIdPauta(idPauta);
	        pauta.setStatusPauta("Pendente");
	        pautaRepository.save(pauta);

	        return "CRIADA";
	    } catch (Exception e) {
	        // Log detalhado da exceção
	        Logger log = LoggerFactory.getLogger(VotacaoService.class);
	        log.error("Erro ao criar pauta com id {}: {}", idPauta, e.getMessage(), e);

	        // Retorna código de erro para o controller
	        return "ERRO";
	    }
	}

	public List<Pauta> listarPautas() {
		return pautaRepository.findAll();
	}

	public String registrarVoto(PautaVotacao voto) {
		Optional<Pauta> pautaOpt = pautaRepository.findById(voto.getId().getIdPauta());
		if (!pautaOpt.isPresent()) {
			return "NAO_ENCONTRADA";
		}

		Pauta pauta = pautaOpt.get();
		if (!"Aberta".equalsIgnoreCase(pauta.getStatusPauta())) {
			return "FECHADA";
		}

		// Verifica se o CPF já votou nessa pauta
		Optional<PautaVotacao> votoExistente = pautaVotacaoRepository
				.findByIdIdPautaAndIdCodCpf(voto.getId().getIdPauta(), voto.getId().getCodCpf());
		if (votoExistente.isPresent()) {
			return "CPF_DUPLICADO";
		}

		pautaVotacaoRepository.save(voto);
		return "REGISTRADO";
	}

	public List<PautaVotacao> listarVotosPorPauta(String idPauta) {
		return pautaVotacaoRepository.findByIdIdPauta(idPauta);
	}

	public String buscarPautaAberta() {
		return pautaRepository.findByStatusPauta("Aberta").map(Pauta::getIdPauta).orElse(null);

	}

	// Busca o id da pauta pendente de abertura
	public String buscarIdPautaAberta() {
	    try {
	        return pautaRepository.findByStatusPauta("Aberta")
	                              .map(Pauta::getIdPauta)
	                              .orElse(null);
	    } catch (Exception e) {
	        Logger log = LoggerFactory.getLogger(VotacaoService.class);
	        log.error("Erro ao buscar pauta aberta: {}", e.getMessage(), e);

	        // Retorna um código especial para o controller identificar
	        return "ERRO";
	    }
	}
	
	public List<Pauta> buscarPautasPendentes() {
	    return pautaRepository.findAllByStatusPauta("Pendente");
	}
	
	public List<Pauta> buscarPautasFechadas() {
		return pautaRepository.findAllByStatusPauta("Fechada");
	}
	
	public String abrirSessao(String idPauta, Integer duracaoMinutos) {
	    try {
	        Optional<Pauta> pautaOpt = pautaRepository.findById(idPauta);
	        if (!pautaOpt.isPresent()) {
	            return "NAO_ENCONTRADA";
	        }

	        // Se já existe alguma pauta aberta, não pode abrir nova sessão
	        Optional<Pauta> pautaAberta = pautaRepository.findByStatusPauta("Aberta");
	        if (pautaAberta.isPresent()) {
	            return "JA_ABERTA";
	        }

	        Pauta pauta = pautaOpt.get();

	        // Se já estiver fechada, não pode abrir novamente
	        if ("Fechada".equalsIgnoreCase(pauta.getStatusPauta())) {
	            return "FECHADA";
	        }

	        // Define duração: se não informado, usa valor default
	        int duracao = (duracaoMinutos != null) ? duracaoMinutos : duracaoDefault;

	        pauta.setStatusPauta("Aberta");
	        pauta.setInicioSessao(LocalDateTime.now());
	        pauta.setFimSessao(LocalDateTime.now().plusMinutes(duracao));
	        pautaRepository.save(pauta);

	        // agenda fechamento automático
	        scheduler.schedule(() -> {
	            try {
	                pauta.setStatusPauta("Fechada");
	                pautaRepository.save(pauta);
	            } catch (Exception e) {
	                log.error("Erro ao fechar pauta automaticamente: {}", e.getMessage(), e);
	            }
	        }, Date.from(pauta.getFimSessao().atZone(ZoneId.systemDefault()).toInstant()));

	        return "ABERTA";
	    } catch (Exception e) {
	        log.error("Erro ao abrir sessão para pauta {}: {}", idPauta, e.getMessage(), e);
	        return "ERRO";
	    }
	}

	public ResponseEntity<Map<String, String>> processarVoto(PautaVotacao voto) {
		String resultado = registrarVoto(voto);
		Map<String, String> resposta = new HashMap<>();
		
		switch (resultado) {
		case "NAO_ENCONTRADA":
			resposta.put("mensagem", "Pauta não encontrada.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
		case "FECHADA":
			resposta.put("mensagem", "Não é possível votar em uma pauta fechada.");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
		case "CPF_DUPLICADO":
			resposta.put("mensagem", "Este CPF já votou nesta pauta.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
		case "REGISTRADO":
			resposta.put("mensagem", "Voto registrado com sucesso!");
			return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
		default:
			resposta.put("mensagem", "Erro ao registrar voto.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
		}
	}

	public String gerarCpfAleatorio() {
	    Random random = new Random();
	    int n1 = random.nextInt(10);
	    int n2 = random.nextInt(10);
	    int n3 = random.nextInt(10);
	    int n4 = random.nextInt(10);
	    int n5 = random.nextInt(10);
	    int n6 = random.nextInt(10);
	    int n7 = random.nextInt(10);
	    int n8 = random.nextInt(10);
	    int n9 = random.nextInt(10);
	
	    // Dígito 1
	    int d1 = 11 - ((n1*10 + n2*9 + n3*8 + n4*7 + n5*6 + n6*5 + n7*4 + n8*3 + n9*2) % 11);
	    d1 = (d1 > 9) ? 0 : d1;
	
	    // Dígito 2
	    int d2 = 11 - ((n1*11 + n2*10 + n3*9 + n4*8 + n5*7 + n6*6 + n7*5 + n8*4 + n9*3 + d1*2) % 11);
	    d2 = (d2 > 9) ? 0 : d2;
	
	    // Retorna apenas os números, sem pontos ou traço
	    return String.format("%d%d%d%d%d%d%d%d%d%d%d", n1,n2,n3,n4,n5,n6,n7,n8,n9,d1,d2);
	}
	
	public Map<String, Object> obterResultadoPauta(String idPauta) {
	    try {
	        Optional<Pauta> pautaOpt = pautaRepository.findById(idPauta);

	        if (!pautaOpt.isPresent()) {
	            Map<String, Object> resposta = new HashMap<>();
	            resposta.put("erro", "NAO_ENCONTRADA");
	            return resposta;
	        }

	        Pauta pauta = pautaOpt.get();

	        if (!"Fechada".equalsIgnoreCase(pauta.getStatusPauta())) {
	            Map<String, Object> resposta = new HashMap<>();
	            resposta.put("erro", "NAO_FECHADA");
	            return resposta;
	        }

	        long qtdSim = pautaVotacaoRepository.countByIdIdPautaAndVoto(idPauta, "SIM");
	        long qtdNao = pautaVotacaoRepository.countByIdIdPautaAndVoto(idPauta, "NAO");

	        Map<String, Object> resposta = new LinkedHashMap<>();
	        resposta.put("idPauta", idPauta);
	        resposta.put("Sim", qtdSim);
	        resposta.put("Não", qtdNao);

	        return resposta;
	    } catch (Exception e) {
	        log.error("Erro ao obter resultado da pauta {}: {}", idPauta, e.getMessage(), e);
	        Map<String, Object> resposta = new HashMap<>();
	        resposta.put("erro", "ERRO");
	        return resposta;
	    }
	}
	
	public ResponseEntity<Map<String, String>> votar(String cpf, String tipoVoto) {
	    log.debug("Request recebido em /votar/{}: {}", tipoVoto.toLowerCase(), cpf);

	    Map<String, String> resposta = new HashMap<>();

	    // Monta a URL a partir da propriedade e chama o serviço de vaçidação do cpf
	    String url = urlValidaCpf + cpf;

	    ResponseEntity<Map<String, String>> response;
	    try {
	        response = restTemplate.exchange(
	            url,
	            HttpMethod.GET,
	            null,
	            new ParameterizedTypeReference<Map<String, String>>() {}
	        );
	    } catch (HttpClientErrorException.NotFound e) {
	        resposta.put("mensagem", "CPF inválido");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	    } catch (Exception e) {
	        resposta.put("mensagem", "Erro ao validar CPF");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	    }

	    // Verifica o JSON retornado
	    Map<String, String> body = response.getBody();
	    if (body != null && body.containsKey("status")) {
	        String status = body.get("status");
	        if ("UNABLE_TO_VOTE".equals(status)) {
	            resposta.put("mensagem", "Cpf não está habilitado para votar");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	        }
	        // Se for "ABLE_TO_VOTE", segue normalmente
	    }

	    String idPautaAberta = this.buscarIdPautaAberta();

	    if ("ERRO".equals(idPautaAberta)) {
	        resposta.put("mensagem", "Erro interno ao buscar pauta aberta para votação");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	    }

	    if (idPautaAberta == null) {
	        resposta.put("mensagem", "Não existe pauta aberta para votação");
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	    }

	    // monta a entidade PautaVotacao com chave composta
	    PautaVotacao voto = new PautaVotacao(
	        new PautaVotacaoId(idPautaAberta, cpf),
	        tipoVoto
	    );

	    return this.processarVoto(voto);
	}
	
	//peridocamente verifica se existem pautas abertas e que já expiraram porque se houver reinicio do servidor podem ficar sem o fechamento do scheduler que abriu a sessão
	@Scheduled(fixedRate = 60000)
	public void fecharPautasExpiradas() {
	    List<Pauta> abertas = pautaRepository.findAllByStatusPauta("Aberta");
	    for (Pauta p : abertas) {
	        if (p.getFimSessao().isBefore(LocalDateTime.now())) {
	            p.setStatusPauta("Fechada");
	            pautaRepository.save(p);
	        }
	    }
	}
}
