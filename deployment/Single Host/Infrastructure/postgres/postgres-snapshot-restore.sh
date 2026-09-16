#!/bin/bash
#
# Script to add Postgres data to snapshots, and restore that data
#
#
source /etc/oqm/snapshots/snapshot-restore-base.sh

#echo "$mode"
#echo "$targetDir"

dataDir="$(oqm-config g 'system.dataDir')/db/postgres"
targetDir="$targetDir/data/infra/postgres"

if [ "$mode" == "snapshot" ]; then
	mkdir -p "$targetDir"
	cp -a "$dataDir/." "$targetDir"
else
	rm -rf "$dataDir"/*
	cp -a "$targetDir/." "$dataDir"
fi