#!/bin/bash
source /etc/oqm/backup/backup-restore-base.sh

#echo "$mode"
#echo "$targetDir"

mongoDataDir="/data/oqm/db/mongo"
mongoTargetDir="$targetDir/data/infra/mongodb/"

if [ "$mode" == "backup" ]; then
        mkdir -p "$mongoTargetDir"
        cp -R "$mongoDataDir/." "$mongoTargetDir"
else
        echo ""
fi