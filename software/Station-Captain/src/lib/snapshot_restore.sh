#
# Functions to handle backing up and restoring data
#

function snapRes_snapshot(){
	# stop everything for a clean backup
	services-stop

	# Setup locations
	local snapshotName="snapshot-$(date +"%Y.%m.%d-%H.%M.%S")"
	local compilingDir="$TMP_DIR/snapshots/$snapshotName";
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
		eval "$backupScript --snapshot -d \"$compilingDir\"";
		local result="$?"
		if [ "$result" -ne 0 ]; then
			echo "FAILED: $result";
			# TODO:: end
		fi
	done

	# start services back up
	services-start

	local snapshotLocation=$(oqm-config -g snapshots.location)
	local snapshotArchiveName="$snapshotLocation/$snapshotName.tar.gz"
	mkdir -p "$snapshotLocation"

	#echo "Snapshot archive: $snapshotArchiveName"

	tar -czvf "$snapshotArchiveName" -C "$compilingDir" $(ls -A "$compilingDir")

	rm -rf "$compilingDir"
}

function snapRes_restore(){
	local something=""
	# TODO
}