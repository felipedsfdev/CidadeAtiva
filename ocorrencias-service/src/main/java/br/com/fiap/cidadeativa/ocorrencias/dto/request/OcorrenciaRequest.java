package br.com.fiap.cidadeativa.ocorrencias.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OcorrenciaRequest(
        @Schema(example = "Semáforo apagado")
        @NotBlank @Size(max = 120) String titulo,

        @Schema(example = "Os sinais estão apagados nos dois sentidos do cruzamento.")
        @NotBlank @Size(max = 2000) String descricao,

        @Schema(example = "Mobilidade")
        @NotBlank @Size(max = 80) String categoria,

        @Schema(example = "Av. do Estado, 120, São Paulo - SP")
        @NotBlank @Size(max = 255) String endereco
) {
}
