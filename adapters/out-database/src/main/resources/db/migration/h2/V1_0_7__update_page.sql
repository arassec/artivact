ALTER TABLE av_page DROP COLUMN index_page;
ALTER TABLE av_page
    ADD wip_content_json TEXT;