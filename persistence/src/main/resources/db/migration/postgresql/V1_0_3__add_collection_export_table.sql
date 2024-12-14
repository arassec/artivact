DROP TABLE av_exhibition;

CREATE TABLE av_collection_export
(
    id           VARCHAR(128) PRIMARY KEY,
    version      INTEGER,
    sort_order   INTEGER,
    content_json TEXT
);
