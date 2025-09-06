ALTER TABLE av_page DROP COLUMN index_page;
ALTER TABLE av_page
    ADD dev_content_json TEXT;