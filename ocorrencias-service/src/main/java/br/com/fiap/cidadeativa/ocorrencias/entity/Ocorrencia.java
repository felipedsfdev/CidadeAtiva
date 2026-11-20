package br.com.fiap.cidadeativa.ocorrencias.entity;

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
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "ocorrencias")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String titulo;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String descricao;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String categoria;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String endereco;

    @NotNull
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private Instant dataCriacao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private StatusOcorrencia status;

    @Column(name = "atendimento_vinculado", nullable = false)
    private boolean atendimentoVinculado;

    @Column(name = "ultimo_atendimento_id")
    private Long ultimoAtendimentoId;

    protected Ocorrencia() {
    }

    public Ocorrencia(String titulo, String descricao, String categoria, String endereco) {
        editar(titulo, descricao, categoria, endereco);
        this.dataCriacao = Instant.now().truncatedTo(ChronoUnit.MICROS);
        this.status = StatusOcorrencia.ABERTA;
    }

    public void editar(String titulo, String descricao, String categoria, String endereco) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.endereco = endereco;
    }

    public void vincularAtendimento(Long atendimentoId) {
        this.ultimoAtendimentoId = atendimentoId;
        atualizarAtendimento(StatusOcorrencia.ENCAMINHADA, true);
    }

    public void atualizarAtendimento(StatusOcorrencia status, boolean vinculado) {
        this.status = status;
        this.atendimentoVinculado = vinculado;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getEndereco() {
        return endereco;
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public StatusOcorrencia getStatus() {
        return status;
    }

    public boolean temAtendimento() {
        return atendimentoVinculado;
    }

    public Long getUltimoAtendimentoId() {
        return ultimoAtendimentoId;
    }
}
