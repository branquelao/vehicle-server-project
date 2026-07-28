CREATE TABLE login (
    id BIGSERIAL PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    login_imagem VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    login_criado_em TIMESTAMP NOT NULL,
    login_atualizado_em TIMESTAMP NOT NULL
);

CREATE TABLE carro (
    id BIGSERIAL PRIMARY KEY,
    carro_nome VARCHAR(50) NOT NULL,
    carro_cor VARCHAR(25) NOT NULL,
    carro_ano INT NOT NULL,
    carro_valor FLOAT NOT NULL,
    carro_imagem VARCHAR(200) NOT NULL,
    carro_anunciado_em TIMESTAMP NOT NULL,
    carro_atualizado_em TIMESTAMP NOT NULL,
    login_id BIGINT NOT NULL,
    CONSTRAINT fk_carro_login FOREIGN KEY (login_id) REFERENCES login(id)
);

CREATE TABLE moto (
    id BIGSERIAL PRIMARY KEY,
    moto_nome VARCHAR(50) NOT NULL,
    moto_cor VARCHAR(25) NOT NULL,
    moto_ano INT NOT NULL,
    moto_valor FLOAT NOT NULL,
    moto_imagem VARCHAR(200) NOT NULL,
    moto_anunciada_em TIMESTAMP NOT NULL,
    moto_atualizada_em TIMESTAMP NOT NULL,
    login_id BIGINT NOT NULL,
    CONSTRAINT fk_moto_login FOREIGN KEY (login_id) REFERENCES login(id)
);