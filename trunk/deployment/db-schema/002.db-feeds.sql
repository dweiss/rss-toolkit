
-- Feeds

DROP TABLE feeds;
DROP SEQUENCE feeds_id_seq;

CREATE TABLE feeds (
    id SERIAL PRIMARY KEY,
    url TEXT  NOT NULL  UNIQUE,
    title TEXT,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    status INT8 DEFAULT 0,  -- Status fields: see FeedStatus.class
    status_text TEXT,
    errors INTEGER,
    ignore BOOLEAN DEFAULT false,

    next_fetch TIMESTAMP WITH TIME ZONE,
    last_fetch TIMESTAMP WITH TIME ZONE,
    ttl INT4,
    adaptive_ttl INT4,
    metadata BYTEA
);

CREATE INDEX feeds_next_fetch on feeds (next_fetch);
CREATE INDEX feeds_status on feeds (status);
CREATE INDEX feeds_ignore on feeds (ignore);
