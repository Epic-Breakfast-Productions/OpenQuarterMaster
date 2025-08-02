#!/bin/bash
source /etc/oqm/accountScripts/account-assure-base.sh

if [ -z "$databaseToAssert" ]; then
	echo "No database given."
	exit 2;
fi

adminUser="$(oqm-config g 'infra.mongodb.adminUser')"
adminPass="$(oqm-config g 'infra.mongodb.adminPass')"
# TODO:: add flag to enable more admin level rather than
# TODO:: each step, assert worked
docker exec oqm-infra-mongo mongosh admin -u "$adminUser" -p "$adminPass" --eval "db.createUser({user: \"$usernameToAssert\", pwd: \"$passwordToAssert\", roles: [\"dbAdminAnyDatabase\", \"readWriteAnyDatabase\"]})"
docker exec oqm-infra-mongo mongosh admin -u "$usernameToAssert" -p "$passwordToAssert" --eval "db.getSiblingDB('$databaseToAssert');"
docker exec oqm-infra-mongo mongosh admin -u "$adminUser" -p "$adminPass" --eval "db.grantRolesToUser(\"$usernameToAssert\",[{role:\"dbOwner\", db: \"$databaseToAssert\"}])"



#TODO:: assert was okay
#if [ "$userExists" == "1" ]; then
#	echo "Account already exists."
#else
#	echo "Account did not exist. Creating account.."
#	docker exec oqm-infra-postgres psql postgres -U "oqm_admin" -tXAc "CREATE USER $usernameToAssert WITH CREATEDB PASSWORD '$passwordToAssert'"
#	echo "Account created."
#fi

