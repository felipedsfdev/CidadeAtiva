package br.com.fiap.cidadeativa.atendimentos.dto.response;

import br.com.fiap.cidadeativa.atendimentos.entity.Prioridade;
import br.com.fiap.cidadeativa.atendimentos.entity.StatusAtendimento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record AtendimentoResponse(
        @Schema(example = "2") Long id,
        @Schema(example = "3") Long ocorrenciaId,
        @Schema(example = "Equipe de Sinalização") String responsavel,
        @Schema(example = "Verificar alimentação elétrica e substituir equipamento danificado.") String descricao,
        @Schema(example = "2026-11-19T21:00:00Z") Instant dataAbertura,
        @Schema(example = "EM_ANDAMENTO") StatusAtendimento status,
        @Schema(example = "ALTA") Prioridade prioridade
) {
}
