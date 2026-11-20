package br.com.fiap.cidadeativa.atendimentos.client;

import br.com.fiap.cidadeativa.atendimentos.exception.ApiException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OcorrenciasFallback implements FallbackFactory<OcorrenciasClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OcorrenciasFallback.class);

    @Override
    public OcorrenciasClient create(Throwable causa) {
        return (id, atualizacao) -> {
            for (Throwable erro = causa; erro != null; erro = erro.getCause()) {
                if (erro instanceof FeignException.NotFound) {
                    throw new ApiException(HttpStatus.NOT_FOUND, "Ocorrência %d não encontrada.".formatted(id));
                }
                if (erro instanceof FeignException.Conflict) {
                    throw new ApiException(HttpStatus.CONFLICT,
                            "A ocorrência %d não permite esta alteração de atendimento.".formatted(id));
                }
            }
            LOGGER.warn("Sincronização {} indisponível para ocorrência {} ({})",
                    atualizacao.acao(), id, causa.getClass().getSimpleName());
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O serviço de ocorrências está indisponível. Tente novamente em instantes.");
        };
    }
}
