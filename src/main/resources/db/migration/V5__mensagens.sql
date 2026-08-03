CREATE TABLE conversa (
    id BIGSERIAL PRIMARY KEY,
    veiculo_id BIGINT NOT NULL,
    comprador_id BIGINT NOT NULL,
    vendedor_id BIGINT NOT NULL,
    criada_em TIMESTAMP NOT NULL,
    atualizada_em TIMESTAMP NOT NULL,
    CONSTRAINT fk_conversa_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversa_comprador FOREIGN KEY (comprador_id) REFERENCES login(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversa_vendedor FOREIGN KEY (vendedor_id) REFERENCES login(id) ON DELETE CASCADE,
    CONSTRAINT uq_conversa_veiculo_comprador UNIQUE (veiculo_id, comprador_id)
);

CREATE TABLE mensagem (
    id BIGSERIAL PRIMARY KEY,
    conversa_id BIGINT NOT NULL,
    remetente_id BIGINT NOT NULL,
    conteudo VARCHAR(1000) NOT NULL,
    enviada_em TIMESTAMP NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_mensagem_conversa FOREIGN KEY (conversa_id) REFERENCES conversa(id) ON DELETE CASCADE,
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (remetente_id) REFERENCES login(id) ON DELETE CASCADE
);