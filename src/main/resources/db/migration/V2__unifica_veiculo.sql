-- Tabela unificada de veículos
CREATE TABLE veiculo (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(10) NOT NULL,               -- CARRO ou MOTO

    -- Campos comuns
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    ano_fabricacao INT NOT NULL,
    ano_modelo INT NOT NULL,
    km INT NOT NULL,
    cor VARCHAR(25) NOT NULL,
    combustivel VARCHAR(20) NOT NULL,        -- FLEX, GASOLINA, DIESEL, ELETRICO, HIBRIDO
    cambio VARCHAR(20) NOT NULL,              -- MANUAL, AUTOMATICO, CVT
    unico_dono BOOLEAN NOT NULL DEFAULT FALSE,
    aceita_troca BOOLEAN NOT NULL DEFAULT FALSE,
    estado_conservacao VARCHAR(15) NOT NULL,  -- NOVO, SEMINOVO, USADO
    valor FLOAT NOT NULL,
    descricao VARCHAR(1000),

    -- Específicos de carro (NULL quando tipo = MOTO)
    carroceria VARCHAR(20),                   -- HATCH, SEDAN, SUV, PICAPE, PERUA
    portas INT,
    potencia_cv INT,
    cilindrada_carro VARCHAR(10),             -- ex: "1.0", "2.0"
    blindado BOOLEAN,

    -- Específicos de moto (NULL quando tipo = CARRO)
    cilindrada_moto INT,                      -- em cc
    categoria_moto VARCHAR(20),               -- NAKED, SCOOTER, TRAIL, CUSTOM, ESPORTIVA
    tipo_partida VARCHAR(10),                 -- ELETRICA, PEDAL

    -- Status e metadados
    status VARCHAR(15) NOT NULL DEFAULT 'ATIVO', -- ATIVO, PAUSADO, VENDIDO, EXPIRADO
    anunciado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL,

    login_id BIGINT NOT NULL,
    CONSTRAINT fk_veiculo_login FOREIGN KEY (login_id) REFERENCES login(id)
);

-- Imagens do veículo (múltiplas por anúncio)
CREATE TABLE veiculo_imagem (
    id BIGSERIAL PRIMARY KEY,
    veiculo_id BIGINT NOT NULL,
    url_imagem VARCHAR(200) NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    ordem INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_imagem_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo(id) ON DELETE CASCADE
);

-- Catálogo de opcionais/equipamentos
CREATE TABLE opcional (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Relação N:N entre veículo e opcionais
CREATE TABLE veiculo_opcional (
    veiculo_id BIGINT NOT NULL,
    opcional_id BIGINT NOT NULL,
    PRIMARY KEY (veiculo_id, opcional_id),
    CONSTRAINT fk_vo_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculo(id) ON DELETE CASCADE,
    CONSTRAINT fk_vo_opcional FOREIGN KEY (opcional_id) REFERENCES opcional(id)
);

-- Perfil de vendedor: pessoa física ou loja/revenda
ALTER TABLE login ADD COLUMN tipo_perfil VARCHAR(15) NOT NULL DEFAULT 'PESSOA_FISICA'; -- PESSOA_FISICA, LOJA
ALTER TABLE login ADD COLUMN razao_social VARCHAR(100);   -- só preenchido se LOJA
ALTER TABLE login ADD COLUMN cnpj VARCHAR(18);             -- só preenchido se LOJA

-- Seed inicial do catálogo de opcionais (mais comuns)
INSERT INTO opcional (nome) VALUES
     ('Ar condicionado'), ('Direção hidráulica'), ('Direção elétrica'),
     ('Vidro elétrico'), ('Trava elétrica'), ('Airbag'), ('Freio ABS'),
     ('Central multimídia'), ('Câmera de ré'), ('Sensor de estacionamento'),
     ('Teto solar'), ('Bancos de couro'), ('Piloto automático'),
     ('Rodas de liga leve'), ('Farol de neblina');