package br.com.fiap.cidadeativa.ocorrencias.dto.response;

import br.com.fiap.cidadeativa.ocorrencias.entity.StatusOcorrencia;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record OcorrenciaResponse(
        @Schema(example = "3") Long id,
        @Schema(example = "Semáforo apagado") String titulo,
        @Schema(example = "Os sinais estão apagados nos dois sentidos do cruzamento.") String descricao,
        @Schema(example = "Mobilidade") String categoria,
        @Schema(example = "Av. do Estado, 120, São Paulo - SP") String endereco,
        @Schema(example = "2026-11-19T20:30:00Z") Instant dataCriacao,
        @Schema(example = "EM_ATENDIMENTO") StatusOcorrencia status
) {
}
