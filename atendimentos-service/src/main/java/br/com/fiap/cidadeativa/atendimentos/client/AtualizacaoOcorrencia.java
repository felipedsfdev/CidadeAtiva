package br.com.fiap.cidadeativa.atendimentos.client;

public record AtualizacaoOcorrencia(Long atendimentoId, Acao acao) {

    public enum Acao {
        ATRIBUIR,
        INICIAR,
        CONCLUIR,
        DESVINCULAR
    }
}
