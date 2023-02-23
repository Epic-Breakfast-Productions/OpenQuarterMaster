
function showDialog() {
	dialog --backtitle "$SCRIPT_TITLE" --hfile "oqm-station-captain-help.txt" "$@"
}

function updateSelection() {
	SELECTION=""
	SELECTION=$(cat $USER_SELECT_FILE)
}


#
# User Interaction Functions
#
#
#

function displayBaseOsInfo() {
	# https://medium.com/technology-hits/basic-linux-commands-to-check-hardware-and-system-information-62a4436d40db

	showDialog --infobox "Retrieving system information..." $TINY_HEIGHT $DEFAULT_WIDTH

	showDialog --infobox "Retrieving system information...\nGetting general system info" $TINY_HEIGHT $DEFAULT_WIDTH
	local ipAddrs="$(hostname -I)"
	ipAddrs="$(echo "$ipAddrs" | sed -e 's/^/    /')"
	ipAddrs="${ipAddrs//$'\n'/\\n}"
	local release="$(cat /etc/os-release)"
	release="$(echo "$release" | sed -e 's/^/    /')"
	release="${release//$'\n'/\\n}"
	local uname="$(uname -a)"
	uname="$(echo "$uname" | sed -e 's/^/    /')"
	uname="${uname//$'\n'/\\n}"

	showDialog --infobox "Retrieving system information...\nGetting hardware info" $TINY_HEIGHT $DEFAULT_WIDTH
	local hwInfo="$(hwinfo --short)"
	hwInfo="$(echo "$hwInfo" | sed -e 's/^/    /')"
	hwInfo="${hwInfo//$'\n'/\\n}"
	local usbDevs="$(lsusb)"
	usbDevs="$(echo "$usbDevs" | sed -e 's/^/    /')"
	usbDevs="${usbDevs//$'\n'/\\n}"

	showDialog --infobox "Retrieving system information...\nGetting disk usage info" $TINY_HEIGHT $DEFAULT_WIDTH
	local diskUsage="$(df -H)"
	diskUsage="$(echo "$diskUsage" | sed -e 's/^/    /')"
	diskUsage="${diskUsage//$'\n'/\\n}"

	local sysInfo="Ip Address(es):\n$ipAddrs\n\nOS Info:\n$release\n\n$uname\n\nHardware Info:\n$hwInfo\n\nUSB devices:\n$usbDevs\n\nDisk usage:\n$diskUsage"
	echo "Done retrieving system info."

	showDialog --title "Host OS Info" --msgbox "$sysInfo" $SUPER_TALL_HEIGHT $SUPER_WIDE_WIDTH
}

function displayOqmInstallStatusInfo() {
	showDialog --infobox "Retrieving installation information..." $TINY_HEIGHT $DEFAULT_WIDTH

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

	showDialog --title "Installation Status" \
		--msgbox "$text" $SUPER_TALL_HEIGHT $SUPER_WIDE_WIDTH
}

function getInfo() {
	while true; do
		showDialog --title "Info" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "OQM Installation Status" \
			2 "Host/Base OS" \
			2>$USER_SELECT_FILE

		updateSelection

		case $SELECTION in
		1)
			displayOqmInstallStatusInfo
			;;
		2)
			displayBaseOsInfo
			;;
		*)
			return
			;;
		esac
	done
}

function updateBaseSystem() {
	showDialog --infobox "Updating Base OS. Please wait." $TINY_HEIGHT $DEFAULT_WIDTH
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
		showDialog --title "ERROR: could not update" \
			--msgbox "No recognized command to update with found. Please submit an issue to cover this OS." $TALL_HEIGHT $DEFAULT_WIDTH
	fi

	if [ $resultReturn -ne 0 ]; then
		showDialog --title "ERROR: Failed to update" \
			--msgbox "Error updating. Output from command:\n\n${result}" $TALL_HEIGHT $WIDE_WIDTH
	fi

	showDialog --title "OS Updates Complete" --yesno "Restart?" 6 $DEFAULT_WIDTH

	case $? in
	# TODO:: reboot not always available?
	0)
		reboot
		exitProg
		;;
	*)
		showDialog --title "Updates Complete." --msgbox "" 0 $DEFAULT_WIDTH
		;;
	esac
}

# https://www.cyberciti.biz/faq/how-to-set-up-automatic-updates-for-ubuntu-linux-18-04/
# https://fedoraproject.org/wiki/AutoUpdates
function enableAutoUpdate() {
	echo "TODO"
}
function disableAutoUpdate() {
	echo "TODO"
}

function enableAutomaticOsUpdates() {
	showDialog --infobox "Enabling auto OS updates. Please wait." 3 $DEFAULT_WIDTH
	#TODO:: this

	showDialog --title "Enabled auto OS updates." --msgbox "" 0 $DEFAULT_WIDTH
}

function disableAutomaticOsUpdates() {
	showDialog --infobox "Disabling auto OS updates. Please wait." 3 $DEFAULT_WIDTH
	# TODO:: doublecheck
	#crontab -r "$AUTO_UPDATE_HOST_CRONTAB_FILE"
	showDialog --title "Disabled auto OS updates." --msgbox "" 0 $DEFAULT_WIDTH
}

function baseOsUpdatesDialog() {
	while true; do
		showDialog --title "Base OS" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Update" \
			2 "Enable Automatic OS Updates" \
			3 "Disable Automatic OS Updates" \
			2>$USER_SELECT_FILE

		updateSelection

		case $SELECTION in
		1)
			updateBaseSystem
			;;
		2)
			enableAutomaticOsUpdates
			;;
		3)
			disableAutomaticOsUpdates
			;;
		*)
			return
			;;
		esac
	done
}

function updatesDialog() {
	while true; do
		showDialog --title "Updates" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Host OS Updates" \
			2 "OQM Installation Updates" \
			2>$USER_SELECT_FILE

		updateSelection

		case $SELECTION in
		1)
			baseOsUpdatesDialog
			;;
		2) # TODO
			;;
		*)
			return
			;;
		esac
	done
}

function cleanupDialog() {
	while true; do
		showDialog --title "Cleanup" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Cleanup docker images and resources" \
			2 "RESET data" \
			2>$USER_SELECT_FILE
		updateSelection

		case $SELECTION in
		1)
			showDialog --infobox "Cleaning up docker resources. Please wait." 3 $DEFAULT_WIDTH

			# TODO::: check for any other steps?
			docker system prune --volumes
			docker image prune -a

			showDialog --title "Docker cleanup complete!" --msgbox "" 0 $DEFAULT_WIDTH
			;;
		2)
			showDialog --title "RESET DATA" --yesno "Are you sure? This will erase ALL data used on the system. Configuration will be untouched, but all application data will be gone. It is recommended to backup your data before doing this.\n\nAre you sure?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
			case $? in
			0)
				showDialog --infobox "Resetting all application data. Please wait." 3 $DEFAULT_WIDTH
				systemctl stop open\\x2bquarter\\x2bmaster*

				rm -rf /data/oqm/db/*

				systemctl start open\\x2bquarter\\x2bmaster* --all

				showDialog --title "Data reset." --msgbox "" 0 $DEFAULT_WIDTH
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

function manageInstallDialog() {
	while true; do
		showDialog --title "Manage Install" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
			1 "Select OQM Major Version TODO" \
			2 "Plugins TODO" \
			3 "Cleanup" \
			2>$USER_SELECT_FILE

		updateSelection

		case $SELECTION in
		3)
			cleanupDialog
			;;
		*)
			return
			;;
		esac
	done
}

function mainUi() {
	while true; do
		showDialog --title "Main Menu" \
			--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT 1 "Info / Status" \
			2 "Manage Installation" \
			3 "Backups" \
			4 "Updates" \
			5 "Captain Settings" \
			2>$USER_SELECT_FILE

		updateSelection

		case $SELECTION in
		1)
			getInfo
			;;
		2)
			manageInstallDialog
			;;
		3) # TODO:manage backups
			;;
		4)
			updatesDialog
			;;
		5) # TODO: OQM Captain settings
			;;
		*)
			return
			;;
		esac
	done
}

function initialSetup() {
	echo "Performing initial setup."
	showDialog --infobox "Performing initial setup. Please wait." $TINY_HEIGHT $DEFAULT_WIDTH

	installUpdateInfra

	installUpdateBaseStation

	echo "Initial Setup complete!"
}


function ui.doInteraction(){
	#
	# Check updatedness of this script
	#
	curInstalledCapVersion=""
	packMan_getInstalledVersion curInstalledCapVersion "$SCRIPT_PACKAGE_NAME"
	echo "Station captain installed version: $curInstalledCapVersion"
	latestStatCapRelease="$(needsUpdated "$SCRIPT_PACKAGE_NAME-$curInstalledCapVersion")"
	echo "DEBUG:: has new release return: $latestStatCapRelease"

	if [ "$latestStatCapRelease" = "" ]; then
		echo "Station Captain up to date."
	else
		statCapUpdateInfo=($latestStatCapRelease)
		echo "Station Captain has a new release!"
		echo "DEBUG:: release info: $latestStatCapRelease"
		showDialog --title "Station Captain new Release" --yesno "Station captain has a new release out:\\n${statCapUpdateInfo[0]}\n\nInstall it?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
		case $? in
		0)
			echo "Updating Station captain."
			installFromUrl "${statCapUpdateInfo[1]}"
			echo "Update installed! Please rerun the script."
			exitProg
			;;
		*)
			echo "Not updating Station Captain."
			;;
		esac
		# TODO:: update
	fi

	#
	# Check if we need to setup
	#
	curInstalledBaseStationVersion=""
	packMan_getInstalledVersion curInstalledBaseStationVersion "open+quarter+master-core-base+station"
	echo "Current installed base station version: $curInstalledBaseStationVersion"

	if [ "$curInstalledBaseStationVersion" = "" ]; then
		showDialog --title "Initial Setup" --yesno "It appears that there is no base station installed. Do initial setup with most recent Base Station?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
		case $? in
		0)
			initialSetup
			;;
		*)
			echo "Not performing initial setup."
			;;
		esac
	fi

	#
	# Get major version of base station
	#
	packMan_getInstalledVersion curInstalledBaseStationVersion "open+quarter+master-core-base+station"
	baseStationMajorVersion="$(getMajorVersion "$curInstalledBaseStationVersion")"
	echo "Current installed base station major version: $baseStationMajorVersion"

	baseStationHasUpdates="$(needsUpdated "open+quarter+master-core-base+station-$curInstalledBaseStationVersion" "$baseStationMajorVersion")"

	if [ "$baseStationHasUpdates" = "" ]; then
		echo "Base Station up to date."
	else
		baseStationUpdateInfo=($baseStationHasUpdates)
		echo "Base Station has a new release!"
		echo "DEBUG:: release info: $baseStationHasUpdates"
		showDialog --title "Base Station new Release" --yesno "Base Station has a new release out:\\n${baseStationUpdateInfo[0]}\n\nInstall it?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
		case $? in
		0)
			echo "Updating Base Station."
			installFromUrl "${baseStationUpdateInfo[1]}"
			echo "Update installed!"
			showDialog --title "Finished" --msgbox "Base Station Update Complete." $TINY_HEIGHT $DEFAULT_WIDTH
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
		mainUi
	fi

	exitProg
}