package br.com.fiap.cidadeativa.ocorrencias.controller;

import br.com.fiap.cidadeativa.ocorrencias.dto.request.SincronizarAtendimentoRequest;
import br.com.fiap.cidadeativa.ocorrencias.service.OcorrenciaService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/api/v1/interno/ocorrencias")
public class IntegracaoAtendimentoController {

    private final OcorrenciaService service;

    public IntegracaoAtendimentoController(OcorrenciaService service) {
        this.service = service;
    }

    @PutMapping("/{id}/atendimento")
    public ResponseEntity<Void> sincronizar(@PathVariable @Positive Long id,
                                             @Valid @RequestBody SincronizarAtendimentoRequest request) {
        service.sincronizarAtendimento(id, request.atendimentoId(), request.acao());
        return ResponseEntity.noContent().build();
    }
}
