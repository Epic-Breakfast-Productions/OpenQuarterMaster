#
# Functions to handle backing up and restoring data
#

function backRes_backup(){
	# stop everything for a clean backup
	services-stop

	# Setup locations
	local backupName="backup-$(date +"%Y.%m.%d-%H.%M.%S")"
	local compilingDir="$TMP_DIR/backup/$backupName";
	local configsDir="$compilingDir/configs"
	local serviceConfigsDir="$compilingDir/serviceConfigs"
	local dataDir="$compilingDir/data"

	mkdir -p "$configsDir"
	mkdir -p "$serviceConfigsDir"
	mkdir -p "$dataDir"

	cp -R "$CONFIG_VALUES_DIR/." "$configsDir"
	cp -R "$SERVICE_CONFIG_DIR/." "$serviceConfigsDir"

	#echo "Calling backup scripts"

	for backupScript in "$BACKUP_SCRIPTS_LOC"/*; do
		echo "$backupScript --backup \"$compilingDir\"";
	done

	# start services back up
	services-start

	local backupLocation=$(oqm-config -g backups.location)
	local backupArchiveName="$backupLocation$backupName.tar.gz"
	mkdir -p "$backupLocation"

	#echo "Backup archive: $backupArchiveName"

	tar -czvf "$backupArchiveName" -C "$compilingDir" $(ls -A "$compilingDir")

	rm -rf "$compilingDir"
}

function backRes_restore(){
	local something=""
	# TODO

}