#
# Functions to handle backing up and restoring data
#

function snapRes_snapshot(){
	local snapshotTrigger="$1"
	# stop everything for a clean snapshot
	services-stop

	# Setup locations
	local snapshotName="snapshot-$(date +"%Y.%m.%d-%H.%M.%S")-$snapshotTrigger"
	local compilingDir="$TMP_DIR/snapshots/$snapshotName";
	local configsDir="$compilingDir/config"
	local serviceConfigsDir="$compilingDir/serviceConfigs"
	local dataDir="$compilingDir/data"

	mkdir -p "$configsDir"
	mkdir -p "$serviceConfigsDir"
	mkdir -p "$dataDir"

	cp -R "$CONFIG_DIR/." "$configsDir"
	cp -R "$SERVICE_CONFIG_DIR/." "$serviceConfigsDir"

	#echo "Calling backup scripts"

	for backupScript in "$SNAPSHOT_SCRIPTS_LOC"/*; do
		eval "$backupScript --snapshot -d \"$compilingDir\"";
		local result="$?"
		if [ "$result" -ne 0 ]; then
			echo "FAILED: $result";
			return 1
		fi
	done

	# start services back up
	services-start

	local snapshotLocation=$(oqm-config -g snapshots.location)
	local snapshotArchiveName="$snapshotLocation/$snapshotName.tar.gz"
	mkdir -p "$snapshotLocation"

	#echo "Snapshot archive: $snapshotArchiveName"

	tar -czvf "$snapshotArchiveName" -C "$compilingDir" $(ls -A "$compilingDir")
	if [ "$?" -ne 0 ]; then
		echo "FAILED to compress snapshot files.";
		return 1
	fi

	rm -rf "$compilingDir"
}

function snapRes_restore(){
	local snapshotFile="$1"

	local extractionDir="$TMP_DIR/snapshots/curExtraction"

	rm -rf "$extractionDir"
	mkdir -p "$extractionDir"

	tar -xf "$snapshotFile" -C "$extractionDir"
	if [ "$?" -ne 0 ]; then
		echo "FAILED to extract snapshot data.";
		return 1
	fi

	services-stop

	for backupScript in "$SNAPSHOT_SCRIPTS_LOC"/*; do
		eval "$backupScript --restore -d \"$extractionDir\"";
		local result="$?"
		if [ "$result" -ne 0 ]; then
			echo "FAILED to restore from script \"$backupScript\": $result";
			return 2
		fi
	done

	echo "DEBUG:: restoring configration"
	# TODO:: we forgot about mainConfig
	local restoringConfigs="$extractionDir/config"
	local restoringServiceConfigs="$extractionDir/serviceConfigs"

	rm -rf "$CONFIG_DIR"/*
	cp -R "$restoringConfigs/." "$CONFIG_DIR/."

	rm -rf "$SERVICE_CONFIG_DIR"/*
	cp -R "$restoringServiceConfigs/." "$SERVICE_CONFIG_DIR/."

	#cp -R "$SERVICE_CONFIG_DIR/." "$serviceConfigsDir"

	#echo "DEBUG:: sleeping"
	#sleep 5m

	services-start
}