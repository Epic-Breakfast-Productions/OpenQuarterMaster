
function ui_showDialog() {
	dialog --backtitle "$SCRIPT_TITLE" --hfile "oqm-station-captain-help.txt" "$@"
}

function ui_updateSelection() {
	SELECTION=""
	SELECTION=$(cat $USER_SELECT_FILE)
}

#
# User Interaction Functions
#
#
#

function ui_displayBaseOsInfo() {
	# https://medium.com/technology-hits/basic-linux-commands-to-check-hardware-and-system-information-62a4436d40db

	ui_showDialog --infobox "Retrieving system information..." $TINY_HEIGHT $DEFAULT_WIDTH

	ui_showDialog --infobox "Retrieving system information...\nGetting general system info" $TINY_HEIGHT $DEFAULT_WIDTH
	local ipAddrs="$(hostname -I)"
	ipAddrs="$(hostname).local $ipAddrs"
	ipAddrs="$(echo "$ipAddrs" | sed -e 's/ /\n/g')"
	ipAddrs="$(echo "$ipAddrs" | sed -e 's/^/    /')"
	ipAddrs="${ipAddrs//$'\n'/\\n}"

	local release="$(cat /etc/os-release)"
	release="$(echo "$release" | sed -e 's/^/    /')"
	release="${release//$'\n'/\\n}"
	local uname="$(uname -a)"
	uname="$(echo "$uname" | sed -e 's/^/    /')"
	uname="${uname//$'\n'/\\n}"

	ui_showDialog --infobox "Retrieving system information...\nGetting hardware info" $TINY_HEIGHT $DEFAULT_WIDTH
	local hwInfo="$(hwinfo --short)"
	hwInfo="$(echo "$hwInfo" | sed -e 's/^/    /')"
	hwInfo="${hwInfo//$'\n'/\\n}"
	local usbDevs="$(lsusb)"
	usbDevs="$(echo "$usbDevs" | sed -e 's/^/    /')"
	usbDevs="${usbDevs//$'\n'/\\n}"

	ui_showDialog --infobox "Retrieving system information...\nGetting disk usage info" $TINY_HEIGHT $DEFAULT_WIDTH
	local diskUsage="$(df -H)"
	diskUsage="$(echo "$diskUsage" | sed -e 's/^/    /')"
	diskUsage="${diskUsage//$'\n'/\\n}"

	local sysInfo="Hostname and Ip Address(es):\n$ipAddrs\n\nOS Info:\n$release\n\n$uname\n\nHardware Info:\n$hwInfo\n\nUSB devices:\n$usbDevs\n\nDisk usage:\n$diskUsage"
	echo "Done retrieving system info."

	ui_showDialog --title "Host OS Info" --msgbox "$sysInfo" $SUPER_TALL_HEIGHT $SUPER_WIDE_WIDTH
}

function ui_displayOqmInstallStatusInfo() {
	ui_showDialog --infobox "Retrieving installation information..." $TINY_HEIGHT $DEFAULT_WIDTH

	local text=""

	local installedPackages=($(packMan_getInstalledPackages))
	installedPackages=($(echo "${installedPackages[@]}" | xargs -n1 | sort | xargs))

	pacMan=""
	packMan_determineSystemPackMan pacMan

	local curPackageInfo=""
	for curPackage in "${installedPackages[@]}"; do
		curPackageInfo=""

		case "$pacMan" in
		"apt")
			curPackageInfo="$(apt-cache show "$curPackage")"

			text="${text}${curPackageInfo//$'\n'/\\n}\n"
			# TODO:: filter for relevant info
			;;
		"yum")
			exitProg "Unsupported package management type."
			;;
		*)
			exitProg "Unsupported package management type."
			;;
		esac

		local serviceStatus
		local escapedPackage="$(systemd-escape "$curPackage.service")"
		serviceStatus="$(systemctl status "$escapedPackage")"
		if [ $? == 0 ]; then
			text="${text}${serviceStatus//$'\n'/\\n}"
		fi

		text="${text}\n\n\n"
	done

	# TODO:: use dpkg to sort out what's installed, versions, etc

	ui_showDialog --title "Installation Status" \
		--msgbox "$text" $SUPER_TALL_HEIGHT $SUPER_WIDE_WIDTH
}

function ui_getInfo() {
	while true; do
		ui_showDialog --title "Info" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "OQM Installation Status" \
			2 "Host/Base OS" \
			2>$USER_SELECT_FILE

		ui_updateSelection

		case $SELECTION in
		1)
			ui_displayOqmInstallStatusInfo
			;;
		2)
			ui_displayBaseOsInfo
			;;
		*)
			return
			;;
		esac
	done
}

function ui_updateBaseSystem() {
	ui_showDialog --infobox "Updating Base OS. Please wait." $TINY_HEIGHT $DEFAULT_WIDTH
	# update base system, based on what distro we are on
	result=""
	resultReturn=0
	# TODO::: why no work? error during apt, but no err captured
	# TODO:: update to new function for determinig system type
	if [ -n "$(command -v yum)" ]; then
		result="$(yum update -y)"
		resultReturn=$?
	elif [ -n "$(command -v apt)" ]; then
		result="$(bash -c 'apt update && apt dist-upgrade -y' 2>&1)"
		resultReturn=$?
	else
		ui_showDialog --title "ERROR: could not update" \
			--msgbox "No recognized command to update with found. Please submit an issue to cover this OS." $TALL_HEIGHT $DEFAULT_WIDTH
	fi

	if [ $resultReturn -ne 0 ]; then
		ui_showDialog --title "ERROR: Failed to update" \
			--msgbox "Error updating. Output from command:\n\n${result}" $TALL_HEIGHT $WIDE_WIDTH
	fi

	ui_showDialog --title "OS Updates Complete" --yesno "Restart?" 6 $DEFAULT_WIDTH

	case $? in
	# TODO:: reboot not always available?
	0)
		reboot
		exitProg
		;;
	*)
		ui_showDialog --title "Updates Complete." --msgbox "" 0 $DEFAULT_WIDTH
		;;
	esac
}

# https://www.cyberciti.biz/faq/how-to-set-up-automatic-updates-for-ubuntu-linux-18-04/
# https://fedoraproject.org/wiki/AutoUpdates
function ui_enableAutoUpdate() {
	echo "TODO"
}
function ui_disableAutoUpdate() {
	echo "TODO"
}

function ui_enableAutomaticOsUpdates() {
	ui_showDialog --infobox "Enabling auto OS updates. Please wait." 3 $DEFAULT_WIDTH
	#TODO:: this

	ui_showDialog --title "Enabled auto OS updates." --msgbox "" 0 $DEFAULT_WIDTH
}

function ui_disableAutomaticOsUpdates() {
	ui_showDialog --infobox "Disabling auto OS updates. Please wait." 3 $DEFAULT_WIDTH
	# TODO:: doublecheck
	#crontab -r "$AUTO_UPDATE_HOST_CRONTAB_FILE"
	ui_showDialog --title "Disabled auto OS updates." --msgbox "" 0 $DEFAULT_WIDTH
}

function ui_baseOsUpdatesDialog() {
	while true; do
		ui_showDialog --title "Base OS" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Update" \
			2 "Enable Automatic OS Updates" \
			3 "Disable Automatic OS Updates" \
			2>$USER_SELECT_FILE

		ui_updateSelection

		case $SELECTION in
		1)
			ui_updateBaseSystem
			;;
		2)
			ui_enableAutomaticOsUpdates
			;;
		3)
			ui_disableAutomaticOsUpdates
			;;
		*)
			return
			;;
		esac
	done
}

function ui_updatesDialog() {
	while true; do
		ui_showDialog --title "Updates" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Host OS Updates" \
			2 "OQM Installation Updates" \
			2>$USER_SELECT_FILE

		ui_updateSelection

		case $SELECTION in
		1)
			ui_baseOsUpdatesDialog
			;;
		2) # TODO
			;;
		*)
			return
			;;
		esac
	done
}

function ui_cleanupDialog() {
	while true; do
		ui_showDialog --title "Cleanup" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Cleanup docker images and resources" \
			2 "RESET data" \
			2>$USER_SELECT_FILE
		ui_updateSelection

		case $SELECTION in
		1)
			ui_showDialog --infobox "Cleaning up docker resources. Please wait." 3 $DEFAULT_WIDTH

			# TODO::: check for any other steps?
			docker system prune --volumes
			docker image prune -a

			ui_showDialog --title "Docker cleanup complete!" --msgbox "" 0 $DEFAULT_WIDTH
			;;
		2)
			ui_showDialog --title "RESET DATA" --yesno "Are you sure? This will erase ALL data used on the system. Configuration will be untouched, but all application data will be gone. It is recommended to backup your data before doing this.\n\nAre you sure?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
			case $? in
			0)
				ui_showDialog --infobox "Resetting all application data. Please wait." 3 $DEFAULT_WIDTH

				services-stop

				files-clearData

				services-start

				ui_showDialog --title "Data reset." --msgbox "" 0 $DEFAULT_WIDTH
				;;
			*)
				echo "Not resetting data."
				;;
			esac
			;;
		*)
			return
			;;
		esac
	done
}
function ui_uninstallDialog() {
	while true; do
		local options=( \
		);
		dialog --title "Uninstall" \
			--separate-output \
			--ok-label "Uninstall" \
			--checklist "Select options:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			0 "Clear App Data" 'off' \
			1 "Clear configuration" 'off' \
			2 "Uninstall Station Captain" 'off' \
			2>$USER_SELECT_FILE
		case $? in
		0)
			ui_updateSelection
			local choices="$SELECTION"
			echo "Choices made: $choices"

			ui_showDialog --infobox "Uninstalling. Please wait." 3 $DEFAULT_WIDTH

			local clearData="false"
			local clearConfig="false"
			local uninstallThis="false"

			if [[ "${choices,,}" == *"0"* ]]; then
				clearData="true"
			fi
			if [[ "${choices,,}" == *"1"* ]]; then
				clearConfig="true"
			fi
			if [[ "${choices,,}" == *"2"* ]]; then
				uninstallThis="true"
			fi

			packMan_uninstallAll $clearData $clearConfig $uninstallThis

			ui_showDialog --title "Finished uninstalling." --msgbox "" 0 $DEFAULT_WIDTH
			;;
		*)
			echo "Not uninstalling."
			ui_showDialog --title "Uninstall Canceled." --msgbox "" 0 $DEFAULT_WIDTH
			;;
		esac
		return;
	done
}

function ui_manageInstallDialog() {
	while true; do
		ui_showDialog --title "Manage Install" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Select OQM Major Version TODO" \
			2 "Plugins TODO" \
			3 "Cleanup" \
			4 "Uninstall All" \
			2>$USER_SELECT_FILE

		ui_updateSelection

		case $SELECTION in
		3)
			ui_cleanupDialog
			;;
		4)
			ui_uninstallDialog
			;;
		*)
			return
			;;
		esac
	done
}


function ui_backupsDialog() {
	while true; do
		ui_showDialog --title "Backups" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Trigger Backups Now" \
			2 "Enable/disable automatic backups TODO" \
			3 "Set backup location TODO" \
			4 "Set number of backups to keep TODO" \
			4 "Set backup frequency TODO" \
			2>$USER_SELECT_FILE
		ui_updateSelection

		case $SELECTION in
		1)
			ui_showDialog --infobox "Performing backup, please wait." 3 $DEFAULT_WIDTH
			backRes_backup
			ui_showDialog --title "Finished backing up." --msgbox "" 0 $DEFAULT_WIDTH
			;;
		*)
			return
			;;
		esac
	done
}

function ui_mainUi() {
	while true; do
		ui_showDialog --title "Main Menu" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT 1 "Info / Status" \
			2 "Manage Installation" \
			3 "Backups" \
			4 "Updates" \
			5 "Captain Settings" \
			2>$USER_SELECT_FILE

		ui_updateSelection

		case $SELECTION in
		1)
			ui_getInfo
			;;
		2)
			ui_manageInstallDialog
			;;
		3)
			ui_backupsDialog
			;;
		4)
			ui_updatesDialog
			;;
		5) # TODO: OQM Captain settings
			;;
		*)
			return
			;;
		esac
	done
}

function ui_initialSetup() {
	echo "Performing initial setup."
	ui_showDialog --infobox "Performing initial setup. Please wait." $TINY_HEIGHT $DEFAULT_WIDTH

	relUtil_installUpdateInfra

	relUtil_installUpdateBaseStation

	echo "Initial Setup complete!"
}


function ui.doInteraction(){
	relUtil_refreshReleaseList

	#
	# Check updatedness of this script
	#
	curInstalledCapVersion=""
	packMan_getInstalledVersion curInstalledCapVersion "$SCRIPT_PACKAGE_NAME"
	echo "Station captain installed version: $curInstalledCapVersion"
	latestStatCapRelease="$(relUtil_needsUpdated "$SCRIPT_PACKAGE_NAME-$curInstalledCapVersion")"
	echo "DEBUG:: has new release return: $latestStatCapRelease"

	if [ "$latestStatCapRelease" = "" ]; then
		echo "Station Captain up to date."
	else
		statCapUpdateInfo=($latestStatCapRelease)
		echo "Station Captain has a new release!"
		echo "DEBUG:: release info: $latestStatCapRelease"
		ui_showDialog --title "Station Captain new Release" --yesno "Station captain has a new release out:\\n${statCapUpdateInfo[0]}\n\nInstall it?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
		case $? in
		0)
			echo "Updating Station captain."
			relUtil_installFromUrl "${statCapUpdateInfo[1]}"
			echo "Update installed! Please rerun the script."
			exitProg
			;;
		*)
			echo "Not updating Station Captain."
			;;
		esac
	fi

	#
	# Check if we need to setup
	#
	curInstalledBaseStationVersion=""
	packMan_getInstalledVersion curInstalledBaseStationVersion "open+quarter+master-core-base+station"
	echo "Current installed base station version: $curInstalledBaseStationVersion"

	if [ "$curInstalledBaseStationVersion" = "" ]; then
		ui_showDialog --title "Initial Setup" --yesno "It appears that there is no base station installed. Do initial setup with most recent Base Station?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
		case $? in
		0)
			ui_initialSetup
			;;
		*)
			echo "Not performing initial setup."
			;;
		esac
	fi

	#
	# Determine if need to update/install infra
	#
	local infraAvailable=($(relUtil_getGitPackagesForType "infra"));
	echo "Infra components available: ${infraAvailable[*]}"

	for curInfra in "${infraAvailable[@]}"; do
		local curInfraFullName="open+quarter+master-$curInfra";
		echo "Checking for install status of $curInfra";

		local curInstalledInfraVersion=""
		packMan_getInstalledVersion curInstalledInfraVersion "$curInfraFullName"

		if [ -z "$curInstalledInfraVersion" ]; then
			echo "$curInfra needs installed";
			ui_showDialog --title "Install $curInfra" --yesno "It appears that $curInfra is not installed. Do install?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
			case $? in
			0)
				local releaseInfo="$(relUtil_getLatestGitReleaseFor "$curInfra")"
				#local releaseInfo="$(relUtil_needsUpdated "$curInfraFullName")"
				local releaseUrl="$(relUtil_getAssetUrlToInstallFromGitRelease "$releaseInfo")"
				relUtil_installFromUrl "$releaseUrl"
				;;
			*)
				echo "Not installing $curInfra."
				;;
			esac
		else
			echo "Cur installed version of $curInfra - $curInstalledInfraVersion"
			local curInfraNeedsUpdates="$(relUtil_needsUpdated "$curInfraFullName-$curInstalledInfraVersion")"
			echo "Need update result: '$curInfraNeedsUpdates'";
			if [ -n "$curInfraNeedsUpdates" ]; then
				echo "$curInfra needs updated.";
				ui_showDialog --title "Update $curInfra" --yesno "It appears that $curInfra has an update out. Do install?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
				case $? in
				0)
					local releaseInfo="$(relUtil_getLatestGitReleaseFor "$curInfra")"
					#local releaseInfo="$(relUtil_needsUpdated "$curInfraFullName")"
					local releaseUrl="$(relUtil_getAssetUrlToInstallFromGitRelease "$releaseInfo")"
					relUtil_installFromUrl "$releaseUrl"
					;;
				*)
					echo "Not updating $curInfra."
					;;
				esac
			else
				echo "$curInfra up to date.";
			fi
		fi
	done

	#
	# Get major version of base station
	#
	packMan_getInstalledVersion curInstalledBaseStationVersion "open+quarter+master-core-base+station"
	baseStationMajorVersion="$(getMajorVersion "$curInstalledBaseStationVersion")"
	echo "Current installed base station major version: $baseStationMajorVersion"

	baseStationHasUpdates="$(relUtil_needsUpdated "open+quarter+master-core-base+station-$curInstalledBaseStationVersion" "$baseStationMajorVersion")"

	if [ "$baseStationHasUpdates" = "" ]; then
		echo "Base Station up to date."
	else
		baseStationUpdateInfo=($baseStationHasUpdates)
		echo "Base Station has a new release!"
		echo "DEBUG:: release info: $baseStationHasUpdates"
		ui_showDialog --title "Base Station new Release" --yesno "Base Station has a new release out:\\n${baseStationUpdateInfo[0]}\n\nInstall it?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
		case $? in
		0)
			echo "Updating Base Station."
			relUtil_installFromUrl "${baseStationUpdateInfo[1]}"
			echo "Update installed!"
			ui_showDialog --title "Finished" --msgbox "Base Station Update Complete." $TINY_HEIGHT $DEFAULT_WIDTH
			;;
		*)
			echo "Not updating base Station."
			;;
		esac
	fi

	#echo "$(compareVersions "Manager-Station_Captain-1.2.4" "Manager-Station_Captain-1.2.4-DEV")"

	#
	# Interact with User
	#
	# TODO:: if get inputs, go to direct mode.
	if [ "$INTERACT_MODE" == "$INTERACT_MODE_UI" ]; then
		ui_mainUi
	fi

	exitProg
}