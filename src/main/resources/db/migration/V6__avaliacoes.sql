CREATE TABLE avaliacao (
    id BIGSERIAL PRIMARY KEY,
    avaliador_id BIGINT NOT NULL,
    avaliado_id BIGINT NOT NULL,
    nota INT NOT NULL,
    comentario VARCHAR(500),
    criada_em TIMESTAMP NOT NULL,
    atualizada_em TIMESTAMP NOT NULL,
    CONSTRAINT fk_avaliacao_avaliador FOREIGN KEY (avaliador_id) REFERENCES login(id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_avaliado FOREIGN KEY (avaliado_id) REFERENCES login(id) ON DELETE CASCADE,
    CONSTRAINT uq_avaliacao_par UNIQUE (avaliador_id, avaliado_id),
    CONSTRAINT chk_avaliacao_nota CHECK (nota BETWEEN 1 AND 5)
);