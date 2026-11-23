CREATE TABLE usuario (
                         id    BIGSERIAL    PRIMARY KEY,
                         nome  VARCHAR(100) NOT NULL,
                         email VARCHAR(150) NOT NULL UNIQUE,
                         senha VARCHAR(255) NOT NULL
);

CREATE TABLE categoria (
                           id         BIGSERIAL   PRIMARY KEY,
                           nome       VARCHAR(50) NOT NULL,
                           grupo      VARCHAR(20) NOT NULL,
                           usuario_id BIGINT      NOT NULL REFERENCES usuario(id),
                           UNIQUE (usuario_id, nome)
);

CREATE TABLE transacao (
                           id           BIGSERIAL      PRIMARY KEY,
                           descricao    VARCHAR(255)   NOT NULL,
                           valor        NUMERIC(12, 2) NOT NULL,
                           tipo         VARCHAR(20)    NOT NULL,
                           data         DATE           NOT NULL,
                           categoria_id BIGINT         NOT NULL REFERENCES categoria(id),
                           usuario_id   BIGINT         NOT NULL REFERENCES usuario(id)
);