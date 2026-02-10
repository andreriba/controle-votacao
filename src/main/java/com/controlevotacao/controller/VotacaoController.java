package com.controlevotacao.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.controlevotacao.service.VotacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/votacao")
public class VotacaoController {

	private final VotacaoService service;
	private static final Logger log = LoggerFactory.getLogger(VotacaoController.class);

	public VotacaoController(VotacaoService service) {
		this.service = service;
	}

	@Operation( summary = "Cria uma nova pauta",
			    description = "Cria uma pauta com o ID informado. Só é permitido criar se não existir outra pauta aberta.",
	    		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
	    		        description = "JSON contendo o ID da pauta a ser criada",
	    		        required = true,
	    		        content = @Content(
	    		            mediaType = "application/json",
	    		            examples = @ExampleObject(value = "{ \"idPauta\": \"123\" }")
	    		        )
	    		)
	) @ApiResponses(value = {
			@ApiResponse(
					responseCode = "201",
					description = "Pauta criada com sucesso",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Pauta criada com sucesso.\" }")
				        )),
			@ApiResponse(
					responseCode = "400",
					description = "Já existe uma pauta aberta",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Já existe uma pauta aberta.\" }")
				        )),
			@ApiResponse(
					responseCode = "409",
					description = "Pauta já existe",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Pauta já existe.\" }")
				        )),
			@ApiResponse(
					responseCode = "500",
					description = "Erro ao criar pauta",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao criar pauta.\" }")
				        ))
	})
	@PostMapping("/criarPauta")
	public ResponseEntity<Map<String, String>> criarPauta(@RequestBody Map<String, String> request) {
	    log.debug("Request recebido em /criarPauta: {}", request);

	    String idPauta = request.get("idPauta");
	    String resultado = service.criarPauta(idPauta);

	    Map<String, String> resposta = new HashMap<>();

	    switch (resultado) {
	        case "EXISTENTE":
	            resposta.put("mensagem", "Pauta já existe.");
	            return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
	        case "CRIADA":
	            resposta.put("mensagem", "Pauta criada com sucesso.");
	            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
	        case "ERRO":
	            resposta.put("mensagem", "Erro interno ao criar pauta.");
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	        default:
	            resposta.put("mensagem", "Erro desconhecido.");
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	    }
	}

	@Operation( summary = "Registrar voto SIM",
			    description = "Registra um voto 'SIM' na pauta aberta. É necessário informar o CPF do votante.",
	    		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
	    		        description = "JSON contendo o CPF do votante",
	    		        required = true,
	    		        content = @Content(
	    		            mediaType = "application/json",
	    		            examples = @ExampleObject(value = "{ \"codCpf\": \"12345678901\" }")
	    		        )
	    		)
	)
	@ApiResponses(value = {
			@ApiResponse(
					responseCode = "201",
					description = "Voto registrado com sucesso",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Voto registrado com sucesso.\" }")
				    )
			),
			@ApiResponse(
					responseCode = "400",
					description = "Não é possível votar em uma pauta fechada",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Não é possível votar em uma pauta fechada.\" }")
				    )
			),
			@ApiResponse(
					responseCode = "404",
					description = "Não existe pauta aberta para votação ou pauta não encontrada",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Não existe pauta aberta para votação.\" }")
				    )
			),
			@ApiResponse(
					responseCode = "409",
					description = "Este CPF já votou nesta pauta",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Este CPF já votou nesta pauta.\" }")
				    )
			),
			@ApiResponse(
					responseCode = "500",
					description = "Erro ao registrar voto",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao registrar voto.\" }")
				    )
			)
	})
	@PostMapping("/votar/sim")    
	public ResponseEntity<Map<String, String>> votarSim(@RequestBody Map<String, String> body) {
	    log.debug("Request recebido em /votar/sim: {}", body);
	    return service.votar(body.get("codCpf"), "SIM");
	}

	@Operation( 
			summary = "Registrar voto NAO",
		    description = "Registra um voto 'NAO' na pauta aberta. É necessário informar o CPF do votante.",
    		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
    		        description = "JSON contendo o CPF do votante",
    		        required = true,
    		        content = @Content(
    		            mediaType = "application/json",
    		            examples = @ExampleObject(value = "{ \"codCpf\": \"12345678901\" }")
    		        )
    		)
	)
	@ApiResponses(value = {
		@ApiResponse(
				responseCode = "201",
				description = "Voto registrado com sucesso",
				content = @Content(
			            mediaType = "application/json",
			            examples = @ExampleObject(value = "{ \"mensagem\": \"Voto registrado com sucesso.\" }")
			    )
		),
		@ApiResponse(
				responseCode = "400",
				description = "Não é possível votar em uma pauta fechada",
				content = @Content(
			            mediaType = "application/json",
			            examples = @ExampleObject(value = "{ \"mensagem\": \"Não é possível votar em uma pauta fechada.\" }")
			    )
		),
		@ApiResponse(
				responseCode = "404",
				description = "Não existe pauta aberta para votação ou pauta não encontrada",
				content = @Content(
			            mediaType = "application/json",
			            examples = @ExampleObject(value = "{ \"mensagem\": \"Não existe pauta aberta para votação.\" }")
			    )
		),
		@ApiResponse(
				responseCode = "409",
				description = "Este CPF já votou nesta pauta",
				content = @Content(
			            mediaType = "application/json",
			            examples = @ExampleObject(value = "{ \"mensagem\": \"Este CPF já votou nesta pauta.\" }")
			    )
		),
		@ApiResponse(
				responseCode = "500",
				description = "Erro ao registrar voto",
				content = @Content(
			            mediaType = "application/json",
			            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao registrar voto.\" }")
			    )
		)
	})
	@PostMapping("/votar/nao")
	public ResponseEntity<Map<String, String>> votarNao(@RequestBody Map<String, String> body) {
	    log.debug("Request recebido em /votar/nao: {}", body);
	    return service.votar(body.get("codCpf"), "NAO");
	}


	@Operation( summary = "Abrir sessão de votação",
			    description = "Abre uma sessão de votação para a pauta informada. Só é permitido abrir se não houver outra sessão aberta.",
	    		requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
	    		        description = "JSON contendo o ID da pauta e opcionalmente a duração da sessão em minutos",
	    		        required = true,
	    		        content = @Content(
	    		            mediaType = "application/json",
	    		            examples = @ExampleObject(
	    		                value = "{ \"idPauta\": \"123\", \"duracao\": \"30\" }"
	    		            )
	    		        )
	    		)
	)
	@ApiResponses(value = {
			@ApiResponse(
				responseCode = "200",
				description = "Sessão aberta com sucesso",
				content = @Content(
			            mediaType = "application/json",
			            examples = @ExampleObject(value = "{ \"mensagem\": \"Sessão aberta com sucesso.\" }")
			        )),
			@ApiResponse(
				responseCode = "400",
				description = "Não é possível abrir uma pauta já fechada ou já existe uma sessão aberta",
				content = @Content(
			            mediaType = "application/json",
			            examples = {
			                @ExampleObject(name = "Pauta fechada", value = "{ \"mensagem\": \"Não é possível abrir uma pauta já fechada ou já existe uma sessão aberta.\" }"),
			            }
			        )),
			@ApiResponse(
				responseCode = "404",
				description = "Pauta não encontrada",
				content = @Content(
			            mediaType = "application/json",
			            examples = @ExampleObject(value = "{ \"mensagem\": \"Pauta não encontrada.\" }")
			        )),
			@ApiResponse(
					responseCode = "500",
					description = "Erro ao abrir sessão",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao abrir sessão.\" }")
				    )
			)
	})
	@PostMapping("/abrirSessao")
	public ResponseEntity<Map<String, String>> abrirSessao(@RequestBody Map<String, String> request) {
	    log.debug("Request recebido em /abrirSessao: {}", request);

	    String idPauta = request.get("idPauta");
	    Integer duracao = request.containsKey("duracao") 
	        ? Integer.parseInt(request.get("duracao")) 
	        : null;

	    String resultado = service.abrirSessao(idPauta, duracao);

	    Map<String, String> resposta = new HashMap<>();

	    switch (resultado) {
	        case "NAO_ENCONTRADA":
	            resposta.put("mensagem", "Pauta não encontrada.");
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	        case "FECHADA":
	            resposta.put("mensagem", "Não é possível abrir uma pauta já fechada.");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	        case "JA_ABERTA":
	            resposta.put("mensagem", "Já existe uma sessão aberta.");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	        case "ABERTA":
	            resposta.put("mensagem", "Sessão aberta com sucesso.");
	            return ResponseEntity.ok(resposta);
	        case "ERRO":
	            resposta.put("mensagem", "Erro interno ao abrir sessão.");
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	        default:
	            resposta.put("mensagem", "Erro desconhecido.");
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	    }
	}
	
	@Operation( summary = "Cancelar operação",
			    description = "Cancela a operação em andamento e retorna uma mensagem informando que o cancelamento foi realizado com sucesso."
	) @ApiResponses(value = { 
			@ApiResponse(
					responseCode = "200",
					description = "Operação cancelada com sucesso", 
					content = @Content(
					            mediaType = "application/json",
					            examples = @ExampleObject(value = "{ \"mensagem\": \"Operação cancelada com sucesso.\" }")
					)
			),
			@ApiResponse(
					responseCode = "500", description = "Erro ao cancelar a operação",
					content = @Content(
				            mediaType = "application/json",
				            examples = @ExampleObject(value = "{ \"mensagem\": \"Erro ao cancelar a operação.\" }")
				    )
			)
	})
	@PostMapping("/cancelar")
	public ResponseEntity<Map<String, String>> cancelarOperacao() {
		Map<String, String> resposta = new HashMap<>();
		resposta.put("mensagem", "Operação cancelada com sucesso.");
		return ResponseEntity.status(HttpStatus.OK).body(resposta);
	}
	
	@Operation(
		    summary = "Obter resultado da pauta",
		    description = "Recebe o idPauta e retorna a quantidade de votos SIM e NÃO. " +
		                  "Se a pauta não existir retorna 404. Se não estiver fechada retorna 400.",
		    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        	description = "JSON contendo o ID da pauta a ser consultada",
        	required = true,
    	        content = @Content(
    	            mediaType = "application/json",
    	            examples = @ExampleObject(
    	                value = "{ \"idPauta\": \"pauta1\" }"
    	            )
    	        )
    	    )
	)
	@ApiResponses(value = {
		    @ApiResponse(
		        responseCode = "200",
		        description = "Resultado retornado com sucesso",
		        content = @Content(
		            mediaType = "application/json",
		            examples = @ExampleObject(
		                value = "{ \"idPauta\": \"pauta1\", \"Sim\": \"10\", \"Não\": \"5\" }"
		            )
		        )
		    ),
		    @ApiResponse(
		        responseCode = "404",
		        description = "Pauta não encontrada",
		        content = @Content(
		            mediaType = "application/json",
		            examples = @ExampleObject(
		                value = "{ \"mensagem\": \"Pauta não encontrada.\" }"
		            )
		        )
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Pauta não está fechada",
		        content = @Content(
		            mediaType = "application/json",
		            examples = @ExampleObject(
		                value = "{ \"mensagem\": \"A pauta não está fechada.\" }"
		            )
		        )
		    ),
		    @ApiResponse(
		        responseCode = "500",
		        description = "Erro interno ao obter resultado",
		        content = @Content(
		            mediaType = "application/json",
		            examples = @ExampleObject(
		                value = "{ \"mensagem\": \"Erro ao obter resultado da pauta.\" }"
		            )
		        )
		    )
	})
	@PostMapping("/resultadoPauta")
	public ResponseEntity<Map<String, String>> resultadoPauta(@RequestBody Map<String, String> request) {
	    log.debug("Request recebido em /resultadoPauta: {}", request);

	    String idPauta = request.get("idPauta");
	    Map<String, Object> resultado = service.obterResultadoPauta(idPauta);

	    Map<String, String> resposta = new HashMap<>();

	    if (resultado.containsKey("erro")) {
	        String erro = (String) resultado.get("erro");
	        switch (erro) {
	            case "NAO_ENCONTRADA":
	                resposta.put("mensagem", "Pauta não encontrada.");
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
	            case "NAO_FECHADA":
	                resposta.put("mensagem", "A pauta não está fechada.");
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
	            case "ERRO":
	                resposta.put("mensagem", "Erro interno ao obter resultado da pauta.");
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	            default:
	                resposta.put("mensagem", "Erro desconhecido.");
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
	        }
	    }

	    // Se não houve erro, retorna o resultado com os votos
	    Map<String, String> sucesso = new LinkedHashMap<>();
	    sucesso.put("idPauta", (String) resultado.get("idPauta"));
	    sucesso.put("Sim", String.valueOf(resultado.get("Sim")));
	    sucesso.put("Não", String.valueOf(resultado.get("Não")));

	    return ResponseEntity.ok(sucesso);
	}
}
