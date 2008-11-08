
-- Articles

DROP TABLE articles;
DROP SEQUENCE article_id_seq;

CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    url TEXT,
    title TEXT,
    content TEXT,
    fk_feed_id SERIAL REFERENCES feeds ON DELETE CASCADE,
    md5 VARCHAR(32),
    posted_at timestamp with time zone,
    added_at timestamp with time zone
);

CREATE INDEX articles_posted_at_index ON articles (posted_at);
CREATE INDEX articles_md5_index ON articles (md5);
CREATE INDEX articles_added_at_index ON articles (added_at);
