package com.controlevotacao.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.controlevotacao.exception.PautaAbertaNaoEncontradaException;
import com.controlevotacao.model.Pauta;
import com.controlevotacao.service.VotacaoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/tela")
public class TelasController {

    @Value("${app.url.cadastro.pauta}")
    private String urlCadastroPauta;

    @Value("${app.url.cancelar}")
    private String urlCancelar;

    @Value("${app.url.voto.sim}")
    private String urlVotoSim;

    @Value("${app.url.voto.nao}")
    private String urlVotoNao;
    
    @Value("${app.url.abertura.sessao.pauta}")
    private String urlAberturaPauta;

    @Value("${sessao.votacao.duracao:1}")
    private int duracaoDefault;
    
    @Value("${app.url.resultado.pauta}")
    private String urlResultadoPauta;

    private final VotacaoService service;

    public TelasController(VotacaoService service) {
        this.service = service;
    }

    @Operation(
    		summary = "Obter tela de cadastro de pauta",
    	    description = "Retorna a estrutura da tela de cadastro de pauta montada diretamente no método. " +
    	                  "As URLs dos botões são substituídas dinamicamente pelas configuradas no application.properties."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "Tela de cadastro retornada com sucesso",
	        content = @Content(
	            mediaType = "application/json",
	            examples = @ExampleObject(
	                value = "{\n" +
	                        "  \"tipo\": \"FORMULARIO\",\n" +
	                        "  \"titulo\": \"CADASTRAR PAUTA\",\n" +
	                        "  \"itens\": [\n" +
	                        "    { \"tipo\": \"TEXTO\", \"texto\": \"CADASTRO DE PAUTAS DE VOTAÇÃO\" },\n" +
	                        "    { \"tipo\": \"INPUT_TEXTO\", \"id\": \"idPauta\", \"titulo\": \"Pauta\", \"valor\": \"\" }\n" +
	                        "  ],\n" +
	                        "  \"botaoOK\": { \"texto\": \"Cadastrar\", \"url\": \"http://localhost:50017/controle-votacao/criarPauta\" },\n" +
	                        "  \"botaoCancelar\": { \"texto\": \"Cancelar\", \"url\": \"http://localhost:50017/controle-votacao/cancelar\" }\n" +
	                        "}"
	            )
	        )
	    ),
	    @ApiResponse(
	        responseCode = "500",
	        description = "Erro ao carregar a tela de cadastro",
	        content = @Content(
	            mediaType = "application/json",
	            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao carregar a tela de cadastro.\" }")
	        )
	    )
	})
	@GetMapping("/TelaCadastroPauta")
	public Map<String, Object> getTelaCadastroPauta() {
	    Map<String, Object> response = new LinkedHashMap<>();
	    response.put("tipo", "FORMULARIO");
	    response.put("titulo", "CADASTRAR PAUTA");

	    List<Map<String, Object>> itens = new ArrayList<>();

	    Map<String, Object> texto = new LinkedHashMap<>();
	    texto.put("tipo", "TEXTO");
	    texto.put("texto", "CADASTRO DE PAUTAS DE VOTAÇÃO");
	    itens.add(texto);

	    Map<String, Object> input = new LinkedHashMap<>();
	    input.put("tipo", "INPUT_TEXTO");
	    input.put("id", "idPauta");
	    input.put("titulo", "Pauta");
	    input.put("valor", "");
	    itens.add(input);

	    response.put("itens", itens);

	    Map<String, Object> botaoOK = new LinkedHashMap<>();
	    botaoOK.put("texto", "Cadastrar");
	    botaoOK.put("url", urlCadastroPauta); // substitui dinamicamente
	    response.put("botaoOK", botaoOK);

	    Map<String, Object> botaoCancelar = new LinkedHashMap<>();
	    botaoCancelar.put("texto", "Cancelar");
	    botaoCancelar.put("url", urlCancelar); // substitui dinamicamente
	    response.put("botaoCancelar", botaoCancelar);

	    return response;
	}

    @Operation( 
    		summary = "Obter tela de votação",
    		description = "Retorna a estrutura da tela de votação montada diretamente no método. " +
    						  "As URLs dos botões são substituídas dinamicamente e um CPF aleatório é gerado para simulação."
	)
	@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "Tela de votação retornada com sucesso"),
	    @ApiResponse(responseCode = "404", description = "Não existe pauta aberta para votação"),
	    @ApiResponse(responseCode = "500", description = "Erro ao carregar a tela de votação")
	})
	@GetMapping("/TelaVoto")
	public Map<String, Object> getTelaVoto() {
	    // Busca pauta aberta
	    String pautaAberta = service.buscarPautaAberta();
	    if (pautaAberta == null || pautaAberta.isEmpty()) {
	        throw new PautaAbertaNaoEncontradaException("Não existe pauta aberta para votação");
	    }

	    // Gera CPF aleatório
	    String cpfGerado = service.gerarCpfAleatorio();

	    // Monta estrutura JSON em memória
	    Map<String, Object> response = new LinkedHashMap<>();
	    response.put("tipo", "SELECAO");
	    response.put("titulo", "Pauta " + pautaAberta);

	    List<Map<String, Object>> itens = new ArrayList<>();

	    Map<String, Object> itemSim = new LinkedHashMap<>();
	    itemSim.put("texto", "Sim");
	    itemSim.put("url", urlVotoSim);
	    Map<String, Object> bodySim = new LinkedHashMap<>();
	    bodySim.put("codCpf", cpfGerado);
	    itemSim.put("body", bodySim);
	    itens.add(itemSim);

	    Map<String, Object> itemNao = new LinkedHashMap<>();
	    itemNao.put("texto", "Nao");
	    itemNao.put("url", urlVotoNao);
	    Map<String, Object> bodyNao = new LinkedHashMap<>();
	    bodyNao.put("codCpf", cpfGerado);
	    itemNao.put("body", bodyNao);
	    itens.add(itemNao);

	    response.put("itens", itens);

	    return response;
	}

    
    @Operation( 
    		summary = "Obter tela de abertura de sessão de pauta",
    		description = "Retorna a tela para abertura de sessão de pauta. " + "Se já existir uma pauta aberta, retorna mensagem informativa. " +
                          "Se não houver pautas pendentes, retorna 404. " + 
    		    		  "Caso contrário, retorna a lista de pautas disponíveis para abertura." 
    ) @ApiResponses(value = {
    		@ApiResponse(
    				responseCode = "200",
    				description = "Tela retornada com sucesso ou mensagem de pauta já aberta",
    				content = @Content(
    			            mediaType = "application/json",
    			            examples = @ExampleObject(
    			                value = "{\n" +
    			                        "  \"titulo\": \"Pauta 123\",\n" +
    			                        "  \"itens\": [\n" +
    			                        "    {\n" +
    			                        "      \"texto\": \"Sim\",\n" +
    			                        "      \"url\": \"http://localhost:50017/controle-votacao/votar/sim\",\n" +
    			                        "      \"body\": { \"codCpf\": \"12345678901\" }\n" +
    			                        "    },\n" +
    			                        "    {\n" +
    			                        "      \"texto\": \"Nao\",\n" +
    			                        "      \"url\": \"http://localhost:50017/controle-votacao/votar/nao\",\n" +
    			                        "      \"body\": { \"codCpf\": \"12345678901\" }\n" +
    			                        "    }\n" +
    			                        "  ]\n" +
    			                        "}"
    			            )
    			       )
    		), 
    	    @ApiResponse(
    	    		responseCode = "404",
    	    		description = "Não existem pautas para abrir uma sessão",
    	    		content = @Content(
    	    	            mediaType = "application/json",
    	    	            examples = @ExampleObject(value = "{ \"mensagem\": \"Não existe pauta aberta para votação.\" }")
    	    	    )
    	    ), 
    	    @ApiResponse(
    	    		responseCode = "500",
    	    		description = "Erro ao carregar a tela de abertura de sessão",
    	    		content = @Content(
    	    	            mediaType = "application/json",
    	    	            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao carregar a tela de votação.\" }")
    	    	    )
    	    )
    })
    @GetMapping("/TelaAberturaSessaoPauta")
    public ResponseEntity<Map<String, Object>> getTelaAberturaSessaoPauta() {
        // Verifica se existe pauta aberta
        String pautaAberta = service.buscarPautaAberta();
        if (pautaAberta != null && !pautaAberta.isEmpty()) {
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("mensagem", "Já existe uma pauta aberta.");
            return ResponseEntity.ok(resposta);
        }

        // Busca pautas pendentes
        List<Pauta> pendentes = service.buscarPautasPendentes();

        // Se não existem pautas pendentes, retorna 404
        if (pendentes == null || pendentes.isEmpty()) {
            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("mensagem", "Não existem pautas para abrir uma sessão.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
        }

        // Monta JSON no formato solicitado
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("tipo", "SELECAO");
        json.put("titulo", "Escolha a pauta que deseja abrir");

        List<Map<String, Object>> itens = new ArrayList<>();
        for (Pauta pauta : pendentes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("texto", pauta.getIdPauta());
            item.put("url", urlAberturaPauta);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("idPauta", pauta.getIdPauta());
            body.put("duracao", duracaoDefault);

            item.put("body", body);
            itens.add(item);
        }

        json.put("itens", itens);

        return ResponseEntity.ok(json);
    }
    
    @Operation(
    	    summary = "Obter tela de pautas fechadas",
    	    description = "Retorna a tela das pautas fechadas para escolher uma pauta e obter o resultado. " +
    	                  "Se não existir pauta fechada, retorna mensagem informativa."
    	)
    	@ApiResponses(value = {
    	    @ApiResponse(
    	        responseCode = "200",
    	        description = "Tela de resultado retornada com sucesso",
    	        content = @Content(
    	            mediaType = "application/json",
    	            examples = @ExampleObject(
    	                value = "{\n" +
    	                        "  \"tipo\": \"SELECAO\",\n" +
    	                        "  \"titulo\": \"Escolha a pauta que deseja visualizar o resultado\",\n" +
    	                        "  \"itens\": [\n" +
    	                        "    {\n" +
    	                        "      \"texto\": \"Pauta 123\",\n" +
    	                        "      \"url\": \"http://localhost:50017/controle-votacao/TelaResultadoPauta\",\n" +
    	                        "      \"body\": { \"idPauta\": \"123\" }\n" +
    	                        "    }\n" +
    	                        "  ]\n" +
    	                        "}"
    	            )
    	        )
    	    ),
    	    @ApiResponse(
    	        responseCode = "404",
    	        description = "Não existe pauta fechada",
    	        content = @Content(
    	            mediaType = "application/json",
    	            examples = @ExampleObject(value = "{ \"mensagem\": \"Não existe pauta fechada.\" }")
    	        )
    	    ),
    	    @ApiResponse(
    	        responseCode = "500",
    	        description = "Erro ao carregar tela de resultado",
    	        content = @Content(
    	            mediaType = "application/json",
    	            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao carregar tela de resultado.\" }")
    	        )
    	    )
    	})
    	@GetMapping("/TelaResultadoPauta")
    	public ResponseEntity<Map<String, Object>> getTelaResultadoPauta() {
    	    List<Pauta> pautasFechadas = service.buscarPautasFechadas();

    	    if (pautasFechadas == null || pautasFechadas.isEmpty()) {
    	        Map<String, Object> resposta = new LinkedHashMap<>();
    	        resposta.put("mensagem", "Não existe pauta fechada.");
    	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    	    }

    	    Map<String, Object> json = new LinkedHashMap<>();
    	    json.put("tipo", "SELECAO");
    	    json.put("titulo", "Escolha a pauta que deseja visualizar o resultado");

    	    List<Map<String, Object>> itens = new ArrayList<>();
    	    for (Pauta pauta : pautasFechadas) {
    	        Map<String, Object> item = new LinkedHashMap<>();
    	        item.put("texto", pauta.getIdPauta());
    	        item.put("url", urlResultadoPauta); // configurado no application.properties

    	        Map<String, Object> body = new LinkedHashMap<>();
    	        body.put("idPauta", pauta.getIdPauta());

    	        item.put("body", body);
    	        itens.add(item);
    	    }

    	    json.put("itens", itens);

    	    return ResponseEntity.ok(json);
    	}

}