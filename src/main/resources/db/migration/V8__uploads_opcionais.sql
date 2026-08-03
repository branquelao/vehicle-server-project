CREATE TABLE upload (
    id BIGSERIAL PRIMARY KEY,
    nome_original VARCHAR(255),
    nome_gerado VARCHAR(255) NOT NULL UNIQUE,
    enviado_em TIMESTAMP NOT NULL,
    login_id BIGINT,
    CONSTRAINT fk_upload_login FOREIGN KEY (login_id) REFERENCES login(id) ON DELETE SET NULL
);