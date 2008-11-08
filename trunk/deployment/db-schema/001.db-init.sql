
--
-- DB initialization script (Postgres)
--

CREATE DATABASE feeds ENCODING='UTF8';

CREATE USER rssadmin WITH NOCREATEDB;
ALTER DATABASE feeds OWNER TO rssadmin;
ALTER USER rssadmin WITH ENCRYPTED PASSWORD 'CHANGEME';
GRANT ALL PRIVILEGES ON DATABASE feeds TO rssadmin;

--
-- group for read-only users.
--
CREATE GROUP readers;

GRANT SELECT ON articles TO GROUP readers;
GRANT SELECT ON feeds TO GROUP readers;
GRANT SELECT ON categories TO GROUP readers;
GRANT SELECT ON categories_feeds TO GROUP readers;
GRANT SELECT ON auto_categories_flat TO GROUP readers;
GRANT SELECT ON events TO GROUP readers;

--
-- user with read-only access 
--

CREATE USER rssuser
	WITH PASSWORD 'CHANGEME' 
	NOCREATEDB;

ALTER GROUP readers ADD USER rssuser;
