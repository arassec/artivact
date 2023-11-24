CREATE TABLE av_configuration
(
    id           VARCHAR(128) PRIMARY KEY,
    version      INTEGER,
    content_json TEXT
);

CREATE TABLE av_account
(
    id IDENTITY NOT NULL PRIMARY KEY,
    version  INTEGER,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(500) NOT NULL,
    email    VARCHAR(128),
    roles    VARCHAR(500)
);

CREATE UNIQUE INDEX ix_account_username ON av_account (username);

CREATE TABLE av_page
(
    id           VARCHAR(128) PRIMARY KEY,
    version      INTEGER,
    index_page   boolean NOT NULL,
    content_json TEXT
);

CREATE TABLE av_item
(
    id                 VARCHAR(128) PRIMARY KEY,
    version            INTEGER,
    content_json       TEXT
);
