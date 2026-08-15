CREATE TABLE busca_salva (
    id BIGSERIAL PRIMARY KEY,
    login_id BIGINT NOT NULL,
    tipo VARCHAR(10),
    marca VARCHAR(50),
    modelo VARCHAR(50),
    preco_min FLOAT,
    preco_max FLOAT,
    ano_min INT,
    ano_max INT,
    km_max INT,
    cor VARCHAR(25),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    criada_em TIMESTAMP NOT NULL,
    CONSTRAINT fk_busca_salva_login FOREIGN KEY (login_id) REFERENCES login(id) ON DELETE CASCADE
);

CREATE TABLE alerta (
    id BIGSERIAL PRIMARY KEY,
    busca_salva_id BIGINT NOT NULL,
    veiculo_id BIGINT NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    visualizado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_alerta_busca_salva FOREIGN KEY (busca_salva_id) REFERENCES busca_salva(id) ON DELETE CASCADE,
    CONSTRAINT fk_alerta_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo(id) ON DELETE CASCADE,
    CONSTRAINT uq_alerta_busca_veiculo UNIQUE (busca_salva_id, veiculo_id)
);