#!/bin/bash
source /etc/oqm/backup/snapshot-restore-base.sh

#echo "$mode"
#echo "$targetDir"

mongoDataDir="/data/oqm/db/mongo"
mongoTargetDir="$targetDir/data/infra/mongodb/"

if [ "$mode" == "snapshot" ]; then
        mkdir -p "$mongoTargetDir"
        cp -R "$mongoDataDir/." "$mongoTargetDir"
else
        echo ""
fi