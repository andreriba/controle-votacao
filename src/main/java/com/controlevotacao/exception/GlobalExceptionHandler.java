package com.controlevotacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PautaAbertaNaoEncontradaException.class)
    public ResponseEntity<MensagemResponse> handlePautaAbertaNaoEncontrada(PautaAbertaNaoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new MensagemResponse(ex.getMessage()));
    }
}

