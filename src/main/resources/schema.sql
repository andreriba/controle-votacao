CREATE TABLE IF NOT EXISTS pauta (
    id_pauta VARCHAR(255) PRIMARY KEY,
    status_pauta VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS pauta_votacao (
    id_pauta VARCHAR(255),
    cod_cpf VARCHAR(255),
    voto VARCHAR(255),
    PRIMARY KEY (id_pauta, cod_cpf),
    FOREIGN KEY (id_pauta) REFERENCES pauta(id_pauta)
);
