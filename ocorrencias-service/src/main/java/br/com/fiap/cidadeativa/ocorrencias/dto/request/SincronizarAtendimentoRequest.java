package br.com.fiap.cidadeativa.ocorrencias.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SincronizarAtendimentoRequest(
        @NotNull @Positive Long atendimentoId,
        @Schema(example = "INICIAR") @NotNull AcaoAtendimento acao
) {
}
