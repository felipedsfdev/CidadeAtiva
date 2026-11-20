package br.com.fiap.cidadeativa.atendimentos.service;

import br.com.fiap.cidadeativa.atendimentos.client.AtualizacaoOcorrencia;
import br.com.fiap.cidadeativa.atendimentos.client.AtualizacaoOcorrencia.Acao;
import br.com.fiap.cidadeativa.atendimentos.client.OcorrenciasClient;
import br.com.fiap.cidadeativa.atendimentos.dto.request.AtualizarAtendimentoRequest;
import br.com.fiap.cidadeativa.atendimentos.dto.request.CriarAtendimentoRequest;
import br.com.fiap.cidadeativa.atendimentos.dto.response.AtendimentoResponse;
import br.com.fiap.cidadeativa.atendimentos.entity.Atendimento;
import br.com.fiap.cidadeativa.atendimentos.entity.StatusAtendimento;
import br.com.fiap.cidadeativa.atendimentos.exception.ApiException;
import br.com.fiap.cidadeativa.atendimentos.repository.AtendimentoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

import static br.com.fiap.cidadeativa.atendimentos.entity.StatusAtendimento.ABERTO;
import static br.com.fiap.cidadeativa.atendimentos.entity.StatusAtendimento.CONCLUIDO;
import static br.com.fiap.cidadeativa.atendimentos.entity.StatusAtendimento.EM_ANDAMENTO;

@Service
@Transactional(readOnly = true)
public class AtendimentoService {

    private final AtendimentoRepository repository;
    private final OcorrenciasClient ocorrencias;

    public AtendimentoService(AtendimentoRepository repository, OcorrenciasClient ocorrencias) {
        this.repository = repository;
        this.ocorrencias = ocorrencias;
    }

    public List<AtendimentoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(AtendimentoService::resposta).toList();
    }

    public AtendimentoResponse buscar(Long id) {
        return resposta(repository.findById(id).orElseThrow(() -> naoEncontrado(id)));
    }

    @Transactional
    public AtendimentoResponse criar(CriarAtendimentoRequest request) {
        if (repository.existsByOcorrenciaId(request.ocorrenciaId())) {
            throw atendimentoDuplicado(request.ocorrenciaId());
        }

        var atendimento = new Atendimento(request.ocorrenciaId(), request.responsavel(),
                request.descricao(), request.prioridade());
        try {
            // A restrição UNIQUE reserva a ocorrência antes da chamada remota, inclusive entre réplicas.
            atendimento = repository.saveAndFlush(atendimento);
        } catch (DataIntegrityViolationException erro) {
            if (erro.getMostSpecificCause() instanceof SQLException sql && sql.getErrorCode() == 1062) {
                throw atendimentoDuplicado(request.ocorrenciaId());
            }
            throw erro;
        }
        sincronizar(atendimento, Acao.ATRIBUIR);
        return resposta(atendimento);
    }

    @Transactional
    public AtendimentoResponse atualizar(Long id, AtualizarAtendimentoRequest request) {
        var atendimento = buscarParaAlteracao(id);
        atendimento.editar(request.responsavel(), request.descricao(), request.prioridade());
        return resposta(atendimento);
    }

    @Transactional
    public AtendimentoResponse alterarStatus(Long id, StatusAtendimento destino) {
        var atendimento = buscarParaAlteracao(id);
        var origem = atendimento.getStatus();
        if (origem == destino) {
            return resposta(atendimento);
        }

        Acao acao;
        if (origem == ABERTO && destino == EM_ANDAMENTO) {
            acao = Acao.INICIAR;
        } else if (origem == EM_ANDAMENTO && destino == CONCLUIDO) {
            acao = Acao.CONCLUIR;
        } else {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Transição de %s para %s inválida.".formatted(origem, destino));
        }

        atendimento.alterarStatus(destino);
        repository.flush();
        sincronizar(atendimento, acao);
        return resposta(atendimento);
    }

    @Transactional
    public void excluir(Long id) {
        var atendimento = buscarParaAlteracao(id);
        repository.delete(atendimento);
        repository.flush();
        try {
            sincronizar(atendimento, Acao.DESVINCULAR);
        } catch (ApiException erro) {
            // Uma tentativa anterior pode ter desvinculado o atendimento antes de perder a resposta.
            if (erro.status() != HttpStatus.NOT_FOUND) {
                throw erro;
            }
        }
    }

    private void sincronizar(Atendimento atendimento, Acao acao) {
        ocorrencias.sincronizar(atendimento.getOcorrenciaId(), new AtualizacaoOcorrencia(atendimento.getId(), acao));
    }

    private Atendimento buscarParaAlteracao(Long id) {
        return repository.buscarParaAlteracao(id).orElseThrow(() -> naoEncontrado(id));
    }

    private ApiException naoEncontrado(Long id) {
        return new ApiException(HttpStatus.NOT_FOUND, "Atendimento %d não encontrado.".formatted(id));
    }

    private ApiException atendimentoDuplicado(Long ocorrenciaId) {
        return new ApiException(HttpStatus.CONFLICT,
                "Já existe um atendimento para a ocorrência %d.".formatted(ocorrenciaId));
    }

    private static AtendimentoResponse resposta(Atendimento atendimento) {
        return new AtendimentoResponse(atendimento.getId(), atendimento.getOcorrenciaId(),
                atendimento.getResponsavel(), atendimento.getDescricao(), atendimento.getDataAbertura(),
                atendimento.getStatus(), atendimento.getPrioridade());
    }
}
