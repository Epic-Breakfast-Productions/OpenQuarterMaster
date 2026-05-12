# Postgresql Infra Component

This postgresql instance is currently used by:

 - [Keycloak infra component](../keycloak)

## Helpful commands

 - Check if pg reports itself ready: `pg_isready`: 
 - List databases: `psql postgres -U oqm_admin -tXAc "SELECT datname FROM pg_database;"`
 - Fix collation issues: `psql postgres -U oqm_admin -tXAc "ALTER DATABASE <database> REFRESH COLLATION VERSION;"`


## Resources

 - https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/
 - https://www.postgresql.org/docs/current/backup-file.html