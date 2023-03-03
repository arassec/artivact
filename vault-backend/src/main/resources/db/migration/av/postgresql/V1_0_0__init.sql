CREATE TABLE av_artivact
(
    id                 VARCHAR(128) PRIMARY KEY,
    version            INTEGER,
    scanned            TIMESTAMP,
    title_json         VARCHAR,
    restrictions_json  VARCHAR,
    description_json   VARCHAR,
    properties_json    VARCHAR,
    media_content_json VARCHAR,
    tags_json          VARCHAR
);

CREATE TABLE av_configuration
(
    id           VARCHAR(128) PRIMARY KEY,
    version      INTEGER,
    content_json VARCHAR
);

CREATE TABLE av_account
(
    id        SERIAL       NOT NULL PRIMARY KEY,
    version   INTEGER,
    username  VARCHAR(50)  NOT NULL,
    password  VARCHAR(500) NOT NULL,
    email     VARCHAR(128),
    roles     VARCHAR(500)
);

CREATE UNIQUE INDEX ix_account_username ON av_account (username);
