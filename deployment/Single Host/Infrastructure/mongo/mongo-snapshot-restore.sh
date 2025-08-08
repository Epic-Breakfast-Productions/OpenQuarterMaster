#!/bin/bash
#
# Script to add MongoDb data to snapshots, and restore that data
#
# https://www.mongodb.com/docs/manual/core/backups/#back-up-with-cp-or-rsync
#
source /etc/oqm/snapshots/snapshot-restore-base.sh

#echo "$mode"
#echo "$targetDir"

mongoDataDir="$(oqm-config g 'system.dataDir')/db/mongo"
mongoTargetDir="$targetDir/data/infra/mongodb"

if [ "$mode" == "snapshot" ]; then
	mkdir -p "$mongoTargetDir"
	cp -a "$mongoDataDir/." "$mongoTargetDir"
else
	rm -rf "$mongoDataDir"/*
	cp -a "$mongoTargetDir/." "$mongoDataDir"
fi