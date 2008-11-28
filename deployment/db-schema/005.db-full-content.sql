
--
-- Content of articles (fetched) and synchronization between rows inserted
-- into articles and the full content table.
--

DROP TABLE full_content;

CREATE TABLE full_content (
    article_id INTEGER,
    fetched BOOLEAN DEFAULT FALSE,
    content BYTEA,
    content_type TEXT
);

CREATE INDEX article_id_index ON full_content (article_id);
CREATE INDEX fetched_index ON full_content (fetched);

--
-- Trigger copying data from articles table to the table of articles to be fetched.
-- 
CREATE OR REPLACE FUNCTION trigger_full_content_articles_sync() RETURNS trigger AS $trigger_full_content_articles_sync$
BEGIN
	INSERT INTO full_content (article_id) VALUES (NEW.id);
       	RETURN NULL;
END;
$trigger_full_content_articles_sync$ LANGUAGE plpgsql;

DROP TRIGGER trigger_full_content_articles_sync ON articles;
CREATE TRIGGER trigger_full_content_articles_sync AFTER INSERT ON articles
    FOR EACH ROW EXECUTE PROCEDURE trigger_full_content_articles_sync();
