package br.com.fiap.cidadeativa.atendimentos.dto.request;

import br.com.fiap.cidadeativa.atendimentos.entity.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AtualizarAtendimentoRequest(
        @Schema(example = "Equipe de Sinalização")
        @NotBlank @Size(max = 120) String responsavel,

        @Schema(example = "Verificar alimentação elétrica e substituir equipamento danificado.")
        @NotBlank @Size(max = 2000) String descricao,

        @Schema(example = "ALTA")
        @NotNull Prioridade prioridade
) {
}
