#!/bin/bash
source /etc/oqm/accountScripts/account-assure-base.sh

# https://stackoverflow.com/questions/14549270/check-if-database-exists-in-postgresql-using-shell

# https://stackoverflow.com/questions/8546759/how-to-check-if-a-postgres-user-exists
userExists=$(docker exec oqm_infra_postgres psql postgres -U "oqm-admin" -tXAc "SELECT 1 FROM pg_roles WHERE rolname='$usernameToAssert'")

if [ "$userExists" == "1" ]; then
#	echo "exists!"
else
#	echo "not exists!"
	docker exec oqm_infra_postgres psql postgres -U "oqm-admin" -tXAc "CREATE USER $usernameToAssert WITH CREATEDB PASSWORD '$passwordToAssert'"
fi

