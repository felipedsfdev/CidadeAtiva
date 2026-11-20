package br.com.fiap.cidadeativa.atendimentos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ocorrencias-service", fallbackFactory = OcorrenciasFallback.class)
public interface OcorrenciasClient {

    @PutMapping("/api/v1/interno/ocorrencias/{id}/atendimento")
    void sincronizar(@PathVariable("id") Long id, @RequestBody AtualizacaoOcorrencia atualizacao);
}
