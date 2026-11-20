package br.com.fiap.cidadeativa.atendimentos.dto.request;

import br.com.fiap.cidadeativa.atendimentos.entity.StatusAtendimento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
        @Schema(example = "EM_ANDAMENTO") @NotNull StatusAtendimento status
) {
}
