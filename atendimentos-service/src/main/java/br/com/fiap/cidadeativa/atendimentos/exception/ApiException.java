package br.com.fiap.cidadeativa.atendimentos.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String detalhe) {
        super(detalhe);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
