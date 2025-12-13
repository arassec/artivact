CREATE TABLE av_favorite
(
    username   VARCHAR(50)  NOT NULL,
    item_id    VARCHAR(128) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    PRIMARY KEY (username, item_id)
);

CREATE INDEX ix_favorite_username ON av_favorite (username);
CREATE INDEX ix_favorite_item_id ON av_favorite (item_id);
