CREATE TABLE ocorrencias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(120) NOT NULL,
    descricao VARCHAR(2000) NOT NULL,
    categoria VARCHAR(80) NOT NULL,
    endereco VARCHAR(255) NOT NULL,
    data_criacao DATETIME(6) NOT NULL,
    status VARCHAR(24) NOT NULL,
    atendimento_vinculado BOOLEAN NOT NULL DEFAULT FALSE,
    ultimo_atendimento_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT ck_ocorrencia_status
        CHECK (status IN ('ABERTA', 'ENCAMINHADA', 'EM_ATENDIMENTO', 'RESOLVIDA')),
    CONSTRAINT ck_ocorrencia_vinculo
        CHECK (status = 'RESOLVIDA' OR (atendimento_vinculado = (status <> 'ABERTA'))),
    CONSTRAINT ck_ocorrencia_ultimo_atendimento CHECK (
        (NOT atendimento_vinculado OR ultimo_atendimento_id IS NOT NULL) AND
        (ultimo_atendimento_id IS NULL OR ultimo_atendimento_id > 0)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
