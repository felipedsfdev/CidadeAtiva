INSERT INTO ocorrencias
    (id, titulo, descricao, categoria, endereco, data_criacao, status, atendimento_vinculado, ultimo_atendimento_id)
VALUES
    (1, 'Buraco na via', 'Buraco próximo ao ponto de ônibus.', 'Pavimentação',
     'Rua da Estação, 42, São Paulo - SP', '2026-11-01 12:00:00', 'ABERTA', FALSE, NULL),
    (2, 'Luminária apagada', 'Poste apagado no acesso à praça.', 'Iluminação',
     'Praça das Flores, 10, São Paulo - SP', '2026-11-02 14:00:00', 'RESOLVIDA', TRUE, 1);
