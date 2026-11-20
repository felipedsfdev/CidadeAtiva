package br.com.fiap.cidadeativa.ocorrencias.service;

import br.com.fiap.cidadeativa.ocorrencias.dto.request.AcaoAtendimento;
import br.com.fiap.cidadeativa.ocorrencias.dto.request.OcorrenciaRequest;
import br.com.fiap.cidadeativa.ocorrencias.dto.response.OcorrenciaResponse;
import br.com.fiap.cidadeativa.ocorrencias.entity.Ocorrencia;
import br.com.fiap.cidadeativa.ocorrencias.entity.StatusOcorrencia;
import br.com.fiap.cidadeativa.ocorrencias.exception.ApiException;
import br.com.fiap.cidadeativa.ocorrencias.repository.OcorrenciaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static br.com.fiap.cidadeativa.ocorrencias.entity.StatusOcorrencia.ABERTA;
import static br.com.fiap.cidadeativa.ocorrencias.entity.StatusOcorrencia.EM_ATENDIMENTO;
import static br.com.fiap.cidadeativa.ocorrencias.entity.StatusOcorrencia.ENCAMINHADA;
import static br.com.fiap.cidadeativa.ocorrencias.entity.StatusOcorrencia.RESOLVIDA;

@Service
@Transactional(readOnly = true)
public class OcorrenciaService {

    private final OcorrenciaRepository repository;

    public OcorrenciaService(OcorrenciaRepository repository) {
        this.repository = repository;
    }

    public List<OcorrenciaResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(OcorrenciaService::resposta).toList();
    }

    public OcorrenciaResponse buscar(Long id) {
        return resposta(repository.findById(id).orElseThrow(() -> naoEncontrada(id)));
    }

    @Transactional
    public OcorrenciaResponse criar(OcorrenciaRequest request) {
        var ocorrencia = new Ocorrencia(request.titulo(), request.descricao(), request.categoria(), request.endereco());
        return resposta(repository.save(ocorrencia));
    }

    @Transactional
    public OcorrenciaResponse atualizar(Long id, OcorrenciaRequest request) {
        var ocorrencia = buscarParaAlteracao(id);
        ocorrencia.editar(request.titulo(), request.descricao(), request.categoria(), request.endereco());
        return resposta(ocorrencia);
    }

    @Transactional
    public void excluir(Long id) {
        var ocorrencia = buscarParaAlteracao(id);
        if (ocorrencia.temAtendimento()) {
            throw new ApiException(HttpStatus.CONFLICT, "Exclua o atendimento vinculado antes de excluir a ocorrência.");
        }
        repository.delete(ocorrencia);
    }

    @Transactional
    public void sincronizarAtendimento(Long id, Long atendimentoId, AcaoAtendimento acao) {
        var ocorrencia = buscarParaAlteracao(id);
        if (acao != AcaoAtendimento.ATRIBUIR && !atendimentoId.equals(ocorrencia.getUltimoAtendimentoId())) {
            throw new ApiException(HttpStatus.CONFLICT, "O atendimento informado não corresponde ao vínculo da ocorrência.");
        }
        switch (acao) {
            case ATRIBUIR -> atribuir(ocorrencia, atendimentoId);
            case INICIAR -> transicionar(ocorrencia, ENCAMINHADA, EM_ATENDIMENTO);
            case CONCLUIR -> transicionar(ocorrencia, EM_ATENDIMENTO, RESOLVIDA);
            case DESVINCULAR -> {
                var status = ocorrencia.getStatus() == RESOLVIDA ? RESOLVIDA : ABERTA;
                ocorrencia.atualizarAtendimento(status, false);
            }
        }
    }

    private void atribuir(Ocorrencia ocorrencia, Long atendimentoId) {
        var ultimoId = ocorrencia.getUltimoAtendimentoId();
        if (ocorrencia.getStatus() == ENCAMINHADA && atendimentoId.equals(ultimoId)) {
            return;
        }
        if (ocorrencia.getStatus() != ABERTA && ocorrencia.getStatus() != ENCAMINHADA) {
            throw transicaoInvalida(ocorrencia, ENCAMINHADA);
        }
        if (ultimoId != null && atendimentoId <= ultimoId) {
            throw new ApiException(HttpStatus.CONFLICT, "O identificador de atendimento está desatualizado.");
        }
        // Uma tentativa de criação anterior pode ter chegado à ocorrência antes de sofrer rollback.
        ocorrencia.vincularAtendimento(atendimentoId);
    }

    private void transicionar(Ocorrencia ocorrencia, StatusOcorrencia origem, StatusOcorrencia destino) {
        if (!ocorrencia.temAtendimento() || (ocorrencia.getStatus() != origem && ocorrencia.getStatus() != destino)) {
            throw transicaoInvalida(ocorrencia, destino);
        }
        ocorrencia.atualizarAtendimento(destino, true);
    }

    private ApiException transicaoInvalida(Ocorrencia ocorrencia, StatusOcorrencia destino) {
        return new ApiException(HttpStatus.CONFLICT,
                "A ocorrência no estado " + ocorrencia.getStatus() + " não aceita a transição para " + destino + ".");
    }

    private Ocorrencia buscarParaAlteracao(Long id) {
        return repository.buscarParaAlteracao(id).orElseThrow(() -> naoEncontrada(id));
    }

    private ApiException naoEncontrada(Long id) {
        return new ApiException(HttpStatus.NOT_FOUND, "Ocorrência %d não encontrada.".formatted(id));
    }

    private static OcorrenciaResponse resposta(Ocorrencia ocorrencia) {
        return new OcorrenciaResponse(ocorrencia.getId(), ocorrencia.getTitulo(), ocorrencia.getDescricao(),
                ocorrencia.getCategoria(), ocorrencia.getEndereco(), ocorrencia.getDataCriacao(), ocorrencia.getStatus());
    }
}
