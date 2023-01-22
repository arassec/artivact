ALTER TABLE av_artivact ALTER COLUMN properties RENAME TO properties_json;
ALTER TABLE av_artivact ADD COLUMN tags_json TEXT;
