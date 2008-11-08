
-- Tables storing search/ indexing state.

DROP TABLE indexing_status;

-- Indexing status table.
CREATE TABLE indexing_status (
    field TEXT PRIMARY KEY,
    value TEXT NOT NULL
);
