CREATE TABLE IF NOT EXISTS login (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    carteira FLOAT NOT NULL DEFAULT 0,
    loginImagem VARCHAR(100),
    loginCriadoEm TIMESTAMP NOT NULL,
    loginAtualizadoEm TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS carro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    carroNome VARCHAR(50) NOT NULL,
    carroCor VARCHAR(25) NOT NULL,
    carroAno INT NOT NULL,
    carroValor FLOAT NOT NULL,
    carroImagem VARCHAR(200) NOT NULL,
    carroAnunciadoEm TIMESTAMP NOT NULL,
    carroAtualizadoEm TIMESTAMP NOT NULL,
    login_id BIGINT NOT NULL,
    CONSTRAINT fk_carro_login FOREIGN KEY (login_id) REFERENCES login(id)
);

CREATE TABLE IF NOT EXISTS moto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    motoNome VARCHAR(50) NOT NULL,
    motoCor VARCHAR(25) NOT NULL,
    motoAno INT NOT NULL,
    motoValor FLOAT NOT NULL,
    motoImagem VARCHAR(200) NOT NULL,
    motoAnunciadaEm TIMESTAMP NOT NULL,
    motoAtualizadaEm TIMESTAMP NOT NULL,
    login_id BIGINT NOT NULL,
    CONSTRAINT fk_moto_login FOREIGN KEY (login_id) REFERENCES login(id)
);