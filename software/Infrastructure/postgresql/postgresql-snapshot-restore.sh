#!/bin/bash
#
# Script to add MongoDb data to snapshots, and restore that data
#
# https://www.mongodb.com/docs/manual/core/backups/#back-up-with-cp-or-rsync
#
source /etc/oqm/snapshot/snapshot-restore-base.sh

#echo "$mode"
#echo "$targetDir"

dataDir="/data/oqm/db/postgres"
targetDir="$targetDir/data/infra/postgres"

if [ "$mode" == "snapshot" ]; then
	mkdir -p "$targetDir"
	cp -a "$dataDir/." "$targetDir"
else
	rm -rf "$dataDir"/*
	cp -a "$targetDir/." "$dataDir"
fi