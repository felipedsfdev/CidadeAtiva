CREATE TABLE atendimentos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ocorrencia_id BIGINT NOT NULL,
    responsavel VARCHAR(120) NOT NULL,
    descricao VARCHAR(2000) NOT NULL,
    data_abertura DATETIME(6) NOT NULL,
    status VARCHAR(24) NOT NULL,
    prioridade VARCHAR(12) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_atendimento_ocorrencia UNIQUE (ocorrencia_id),
    CONSTRAINT ck_atendimento_ocorrencia CHECK (ocorrencia_id > 0),
    CONSTRAINT ck_atendimento_status CHECK (status IN ('ABERTO', 'EM_ANDAMENTO', 'CONCLUIDO')),
    CONSTRAINT ck_atendimento_prioridade CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA', 'URGENTE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
