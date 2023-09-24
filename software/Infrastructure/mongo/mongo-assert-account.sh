#!/bin/bash
source /etc/oqm/accountScripts/account-assure-base.sh

if [ -z "$databaseToAssert" ]; then
	echo "No database given."
	exit 2;
fi

adminUser="$(oqm-config -g 'infra.mongodb.adminUser')"
adminPass="$(oqm-config -g 'infra.mongodb.adminPass')"
# TODO:: each step, assert worked
docker exec oqm_infra_mongo mongo admin -u "$adminUser" -p "$adminPass" --eval "db.createUser({user: \"$usernameToAssert\", pwd: \"$passwordToAssert\", roles: []})"
docker exec oqm_infra_mongo mongo admin -u "$usernameToAssert" -p "$passwordToAssert" --eval "db.getSiblingDB('$databaseToAssert');"
docker exec oqm_infra_mongo mongo admin -u "$adminUser" -p "$adminPass" --eval "db.grantRolesToUser(\"$usernameToAssert\",[{role:\"dbOwner\", db: \"$databaseToAssert\"}])"



#TODO:: assert was okay
#if [ "$userExists" == "1" ]; then
#	echo "Account already exists."
#else
#	echo "Account did not exist. Creating account.."
#	docker exec oqm_infra_postgres psql postgres -U "oqm_admin" -tXAc "CREATE USER $usernameToAssert WITH CREATEDB PASSWORD '$passwordToAssert'"
#	echo "Account created."
#fi

