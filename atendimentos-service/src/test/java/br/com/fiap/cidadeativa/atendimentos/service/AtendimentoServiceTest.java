package br.com.fiap.cidadeativa.atendimentos.service;

import br.com.fiap.cidadeativa.atendimentos.client.AtualizacaoOcorrencia;
import br.com.fiap.cidadeativa.atendimentos.client.AtualizacaoOcorrencia.Acao;
import br.com.fiap.cidadeativa.atendimentos.client.OcorrenciasClient;
import br.com.fiap.cidadeativa.atendimentos.dto.request.CriarAtendimentoRequest;
import br.com.fiap.cidadeativa.atendimentos.entity.Atendimento;
import br.com.fiap.cidadeativa.atendimentos.entity.Prioridade;
import br.com.fiap.cidadeativa.atendimentos.exception.ApiException;
import br.com.fiap.cidadeativa.atendimentos.repository.AtendimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static br.com.fiap.cidadeativa.atendimentos.entity.StatusAtendimento.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AtendimentoServiceTest {

    private final AtendimentoRepository repository = mock(AtendimentoRepository.class);
    private final OcorrenciasClient client = mock(OcorrenciasClient.class);
    private final AtendimentoService service = new AtendimentoService(repository, client);
    private final Atendimento atendimento = new Atendimento(2L, "Equipe", "Verificar poste", Prioridade.ALTA);

    @BeforeEach
    void atribuirIdAoAtendimentoPersistido() {
        ReflectionTestUtils.setField(atendimento, "id", 7L);
    }

    @Test
    void criaAtendimentoEEncaminhaOcorrenciaAntesDeRetornar() {
        when(repository.saveAndFlush(any(Atendimento.class))).thenReturn(atendimento);
        var request = new CriarAtendimentoRequest(2L, "Equipe", "Verificar poste", Prioridade.ALTA);

        assertThat(service.criar(request).status()).isEqualTo(ABERTO);

        var ordem = inOrder(repository, client);
        ordem.verify(repository).saveAndFlush(any(Atendimento.class));
        ordem.verify(client).sincronizar(2L, new AtualizacaoOcorrencia(7L, Acao.ATRIBUIR));
    }

    @Test
    void iniciaEConcluiAtendimentoSincronizandoCadaEtapa() {
        when(repository.buscarParaAlteracao(1L)).thenReturn(Optional.of(atendimento));

        assertThat(service.alterarStatus(1L, EM_ANDAMENTO).status()).isEqualTo(EM_ANDAMENTO);
        assertThat(service.alterarStatus(1L, CONCLUIDO).status()).isEqualTo(CONCLUIDO);
        assertThat(service.alterarStatus(1L, CONCLUIDO).status()).isEqualTo(CONCLUIDO);

        var ordem = inOrder(client);
        ordem.verify(client).sincronizar(2L, new AtualizacaoOcorrencia(7L, Acao.INICIAR));
        ordem.verify(client).sincronizar(2L, new AtualizacaoOcorrencia(7L, Acao.CONCLUIR));
        ordem.verifyNoMoreInteractions();
    }

    @Test
    void erroRemotoNoStatusEPropagadoParaRollback() {
        when(repository.buscarParaAlteracao(1L)).thenReturn(Optional.of(atendimento));
        doThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Indisponível"))
                .when(client).sincronizar(2L, new AtualizacaoOcorrencia(7L, Acao.INICIAR));

        assertThatThrownBy(() -> service.alterarStatus(1L, EM_ANDAMENTO))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void rejeitaPularEtapaSemChamarOcorrencias() {
        when(repository.buscarParaAlteracao(1L)).thenReturn(Optional.of(atendimento));

        assertThatThrownBy(() -> service.alterarStatus(1L, CONCLUIDO))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.status()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(atendimento.getStatus()).isEqualTo(ABERTO);
        verifyNoInteractions(client);
    }

    @Test
    void rejeitaSegundoAtendimentoParaMesmaOcorrencia() {
        when(repository.existsByOcorrenciaId(2L)).thenReturn(true);
        var request = new CriarAtendimentoRequest(2L, "Equipe", "Verificar poste", Prioridade.ALTA);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.status()).isEqualTo(HttpStatus.CONFLICT));
        verifyNoInteractions(client);
    }
}
