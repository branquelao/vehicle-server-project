CREATE TABLE favorito (
    login_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    PRIMARY KEY (login_id, veiculo_id),
    CONSTRAINT fk_favorito_login FOREIGN KEY (login_id) REFERENCES login(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorito_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo(id) ON DELETE CASCADE
);