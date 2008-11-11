
--
-- DB initialization script (Postgres)
--

CREATE DATABASE feeds ENCODING='UTF8';

CREATE USER rssadmin WITH NOCREATEDB;
ALTER DATABASE feeds OWNER TO rssadmin;
ALTER USER rssadmin WITH ENCRYPTED PASSWORD 'CHANGEME';
GRANT ALL PRIVILEGES ON DATABASE feeds TO rssadmin;

