package br.com.fiap.cidadeativa.atendimentos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "atendimentos")
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(name = "ocorrencia_id", nullable = false, updatable = false)
    private Long ocorrenciaId;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String responsavel;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String descricao;

    @NotNull
    @Column(name = "data_abertura", nullable = false, updatable = false)
    private Instant dataAbertura;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 24)
    private StatusAtendimento status;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 12)
    private Prioridade prioridade;

    protected Atendimento() {
    }

    public Atendimento(Long ocorrenciaId, String responsavel, String descricao, Prioridade prioridade) {
        this.ocorrenciaId = ocorrenciaId;
        this.dataAbertura = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.status = StatusAtendimento.ABERTO;
        editar(responsavel, descricao, prioridade);
    }

    public void editar(String responsavel, String descricao, Prioridade prioridade) {
        this.responsavel = responsavel;
        this.descricao = descricao;
        this.prioridade = prioridade;
    }

    public void alterarStatus(StatusAtendimento status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getOcorrenciaId() {
        return ocorrenciaId;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getDataAbertura() {
        return dataAbertura;
    }

    public StatusAtendimento getStatus() {
        return status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }
}
