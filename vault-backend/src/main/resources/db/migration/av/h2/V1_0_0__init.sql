CREATE TABLE av_artivact
(
    id                 VARCHAR(128) PRIMARY KEY,
    version            INTEGER,
    scanned            TIMESTAMP,
    title              TEXT,
    restrictions       TEXT,
    description        TEXT,
    properties         TEXT,
    media_content_json TEXT
);

CREATE TABLE av_configuration
(
    id      VARCHAR(128) PRIMARY KEY,
    version INTEGER,
    content TEXT
);

CREATE TABLE av_account
(
    id IDENTITY NOT NULL PRIMARY KEY,
    version   INTEGER,
    username  VARCHAR(50)  NOT NULL,
    password  VARCHAR(500) NOT NULL,
    enabled   boolean      NOT NULL,
    email     VARCHAR(128),
    firstname VARCHAR(128),
    lastname  VARCHAR(128),
    roles     VARCHAR(500)
);

CREATE UNIQUE INDEX ix_account_username ON av_account (username);
