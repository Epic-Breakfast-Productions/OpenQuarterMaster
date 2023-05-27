#!/bin/bash
source /etc/oqm/snapshot/snapshot-restore-base.sh

#echo "$mode"
#echo "$targetDir"

mongoDataDir="/data/oqm/db/mongo"
mongoTargetDir="$targetDir/data/infra/mongodb"

if [ "$mode" == "snapshot" ]; then
	mkdir -p "$mongoTargetDir"
	cp -a "$mongoDataDir/." "$mongoTargetDir"
else
	rm -rf "$mongoDataDir"/*
	cp -a "$mongoTargetDir/." "$mongoDataDir"
fi