package br.com.fiap.cidadeativa.ocorrencias.service;

import br.com.fiap.cidadeativa.ocorrencias.dto.request.AcaoAtendimento;
import br.com.fiap.cidadeativa.ocorrencias.entity.Ocorrencia;
import br.com.fiap.cidadeativa.ocorrencias.exception.ApiException;
import br.com.fiap.cidadeativa.ocorrencias.repository.OcorrenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static br.com.fiap.cidadeativa.ocorrencias.entity.StatusOcorrencia.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OcorrenciaServiceTest {

    private final OcorrenciaRepository repository = mock(OcorrenciaRepository.class);
    private final OcorrenciaService service = new OcorrenciaService(repository);
    private final Ocorrencia ocorrencia = new Ocorrencia("Poste apagado", "Há pouca luz no acesso",
            "Iluminação", "Rua Central, 100");

    @BeforeEach
    void preparar() {
        when(repository.buscarParaAlteracao(1L)).thenReturn(Optional.of(ocorrencia));
    }

    @Test
    void sincronizaTodasAsEtapasEAceitaRepeticao() {
        aplicarDuasVezes(AcaoAtendimento.ATRIBUIR);
        assertThat(ocorrencia.getStatus()).isEqualTo(ENCAMINHADA);
        assertThat(ocorrencia.temAtendimento()).isTrue();

        aplicarDuasVezes(AcaoAtendimento.INICIAR);
        assertThat(ocorrencia.getStatus()).isEqualTo(EM_ATENDIMENTO);

        aplicarDuasVezes(AcaoAtendimento.CONCLUIR);
        assertThat(ocorrencia.getStatus()).isEqualTo(RESOLVIDA);

        aplicarDuasVezes(AcaoAtendimento.DESVINCULAR);
        assertThat(ocorrencia.getStatus()).isEqualTo(RESOLVIDA);
        assertThat(ocorrencia.temAtendimento()).isFalse();

        service.excluir(1L);
        verify(repository).delete(ocorrencia);
    }

    @Test
    void bloqueiaExclusaoComVinculo() {
        service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.ATRIBUIR);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.status()).isEqualTo(HttpStatus.CONFLICT));

        verify(repository, times(2)).buscarParaAlteracao(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void desvincularAtendimentoPendenteReabreOcorrencia() {
        service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.ATRIBUIR);
        service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.INICIAR);
        service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.DESVINCULAR);

        assertThat(ocorrencia.getStatus()).isEqualTo(ABERTA);
        assertThat(ocorrencia.temAtendimento()).isFalse();
    }

    @Test
    void rejeitaConclusaoSemInicio() {
        service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.ATRIBUIR);

        assertThatThrownBy(() -> service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.CONCLUIR))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.status()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(ocorrencia.getStatus()).isEqualTo(ENCAMINHADA);
    }

    @Test
    void retentativaDeCriacaoAssumeVinculoERejeitaChamadaAtrasada() {
        service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.ATRIBUIR);
        service.sincronizarAtendimento(1L, 8L, AcaoAtendimento.ATRIBUIR);

        assertThatThrownBy(() -> service.sincronizarAtendimento(1L, 7L, AcaoAtendimento.DESVINCULAR))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.status()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(ocorrencia.temAtendimento()).isTrue();
        assertThat(ocorrencia.getUltimoAtendimentoId()).isEqualTo(8L);
    }

    private void aplicarDuasVezes(AcaoAtendimento acao) {
        service.sincronizarAtendimento(1L, 7L, acao);
        service.sincronizarAtendimento(1L, 7L, acao);
    }
}
