#!/bin/bash
# This script manages an installation of Open QuarterMaster
#
# Author: Greg Stewart
# https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster
#
# requires:
#  - dialog
#  - docker
#  - hwinfo
#  - sponge (from moreutils)
#  - jq
SCRIPT_VERSION="1.0.6-DEV"
SCRIPT_PACKAGE_NAME="open+quarter+master-manager-station+captain"
SCRIPT_TITLE="Open QuarterMaster Station Captain V${SCRIPT_VERSION}"

# urls
HOME_GIT="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster"
HOME_PLUGIN_REPO="https://raw.githubusercontent.com/Epic-Breakfast-Productions/OpenQuarterMaster/main/software/plugins/plugin-repo.json"
GIT_API_BASE="https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster"
GIT_RELEASES="$GIT_API_BASE/releases"

# files
TMP_DIR="/tmp/oqm"
DOWNLOAD_DIR="$TMP_DIR/download"
SHARED_CONFIG_DIR="/etc/oqm"
META_INFO_DIR="$SHARED_CONFIG_DIR/meta"
RELEASE_LIST_FILE="$META_INFO_DIR/releases.json"
RELEASE_LIST_FILE_WORKING="$META_INFO_DIR/releases_unfinished.json"
RELEASE_VERSIONS_DIR="$META_INFO_DIR/versions"
RELEASE_INFRA_VERSIONS="$RELEASE_VERSIONS_DIR/infra.json"
RELEASE_MNGR_VERSIONS="$RELEASE_VERSIONS_DIR/manager.json"

# Selection
USER_SELECT_FILE="$TMP_DIR/oqm-captain-input"
AUTO_UPDATE_HOST_CRONTAB_FILE="autoUpdateOs"
SELECTION=""
DEFAULT_WIDTH=55
WIDE_WIDTH=80
SUPER_WIDE_WIDTH=160
TINY_HEIGHT=5
DEFAULT_HEIGHT=15
TALL_HEIGHT=30
SUPER_TALL_HEIGHT=60
# How the user is interacting with this script. Either "ui" or "direct"
INTERACT_MODE_UI="ui"
INTERACT_MODE_DIRECT="direct"
INTERACT_MODE="$INTERACT_MODE_UI"

#test -n "$DISPLAY" && DIALOG=xdialog

# Software release name prefixes
SW_PREFIX_STATION_CAP="Manager-Station_Captain"
SW_TYPE_INFRA="Infra"
SW_TYPE_MANAGER="Manager"
SW_TYPE_BASE_STATION="Core"
SW_TYPE_PLUGIN="Plugin"

VERSION_FLAG_PRIORITY=("NIGHTLY" "DEV" "" "FINAL")

VERSION_FLAG_CAP="DEV"

##################################################
# Functions
######################################
###############################
##########################
####################
##############
########
#####
###
#
function showDialog() {
	dialog --backtitle "$SCRIPT_TITLE" --hfile "oqm-station-captain-help.txt" "$@"
}

# TODO:: take arg to return
function exitProg() {
	if [ -f "$USER_SELECT_FILE" ]; then
		rm "$USER_SELECT_FILE"
	fi

	if [ "$1" = "" ]; then
		echo "Exiting."
		clear -x
		exit
	else
		echo "ERROR:: $2"
		showDialog --title "Unrecoverable Error" --msgbox "$2" $TALL_HEIGHT $WIDE_WIDTH
		clear -x
		exit $1
	fi
}

function updateSelection() {
	SELECTION=""
	SELECTION=$(cat $USER_SELECT_FILE)
}

function determineSystemPackMan() {
	local return="$1"
	local result=""
	if [ -n "$(command -v yum)" ]; then
		result="yum"
	elif [ -n "$(command -v apt)" ]; then
		result="apt"
	else
		exitProg 2 "Unable to determine if system uses a supported package manager."
	fi
	eval $return="$result"
}
function determineSystemPackFileFormat() {
	local return="$1"

	pacMan=""
	determineSystemPackMan pacMan

	local result=""

	case "$pacMan" in
	"apt")
		result=".deb"
		;;
	"yum")
		result=".rpm"
		;;
	esac

	eval $return="$result"
}

#
# Version Utility Functions
#
#
#

#
# Compares two version strings. Does not account for flags.
# Usage:  compareVersions "major" "1.2.3" "1.2.3"
# Returns: "> {level}" "< {level}" or "="
# https://unix.stackexchange.com/a/570581/66841
#
function compareVersionNumbersRec() {
	function sub_ver() {
		local len=${#1}
		temp=${1%%"."*} && indexOf=$(echo ${1%%"."*} | echo ${#temp})
		echo -e "${1:0:indexOf}"
	}
	function cut_dot() {
		local offset=${#1}
		local length=${#2}
		echo -e "${2:((++offset)):length}"
	}
	if [ -z "$2" ] || [ -z "$3" ]; then
		echo "="
	else
		local v1=$(echo -e "${2}" | tr -d '[[:space:]]')
		local v2=$(echo -e "${3}" | tr -d '[[:space:]]')
		local v1_sub=$(sub_ver $v1)
		local v2_sub=$(sub_ver $v2)
		if ((v1_sub > v2_sub)); then
			echo "> $1"
		elif ((v1_sub < v2_sub)); then
			echo "< $1"
		else
			local cut1="$(cut_dot $v1_sub $v1)"
			local cut2="$(cut_dot $v2_sub $v2)"
			if [ "$1" = "major" ]; then
				compareVersionNumbersRec "minor" "$cut1" "$cut2"
			elif [ "$1" = "minor" ]; then
				compareVersionNumbersRec "patch" "$cut1" "$cut2"
			else
				compareVersionNumbersRec "" "$cut1" "$cut2"
			fi
		fi
	fi
}

#
# Compares two version strings. Does not account for flags. wrapper for the recursive function.
# Usage:  compareVersions "1.2.3" "1.2.3"
# Returns: "> {level}" "< {level}" or "="
#
function compareVersionNumbers() {
	compareVersionNumbersRec "major" $1 $2
}

#
# Gets the index of an item in an array
# Usage: getIndexInArr "item" "${array[@]}"
#
function getIndexInArr() {
	local needle="$1"
	shift
	local haystack=("$@")
	for i in "${!haystack[@]}"; do
		if [[ "${haystack[$i]}" = "${needle}" ]]; then
			echo "${i}"
			return
		fi
	done
}

#
# Compares release versions
# Usage: compareVersionFlags "type-name-version[-FLAG]" "type-name-version[-FLAG]"
# Returns: "> flag" "< flag" or "="
#
function compareVersionFlags() {
	local flag1="$1"
	local flag2="$2"

	local flagVal1="$(getIndexInArr "$flag1" "${VERSION_FLAG_PRIORITY[@]}")"
	local flagVal2="$(getIndexInArr "$flag2" "${VERSION_FLAG_PRIORITY[@]}")"

	if [ "$flagVal1" -eq "$flagVal2" ]; then
		echo "="
	elif [ "$flagVal1" -gt "$flagVal2" ]; then
		echo "> flag"
	else
		echo "< flag"
	fi
}

#
# Compares release versions
# Usage: compareVersions "type-name-version[-FLAG]" "type-name-version[-FLAG]"
# Returns: "> {level}" "< {level}" or "="
#
function compareVersions() {
	local fullVer1="$1"
	local fullVer2="$2"
	local ver1Arr=(${fullVer1//-/ })
	local ver2Arr=(${fullVer2//-/ })

	if [ "${ver1Arr[0]}" != "${ver2Arr[0]}" ] || [ "${ver1Arr[1]}" != "${ver2Arr[1]}" ]; then
		exitProg 66 "ERROR:: Versions given are not comparable \"$fullVer1\", \"$fullVer2\". Please let the developers know of this issue."
	fi

	local ver1Num="${ver1Arr[2]}"
	local ver2Num="${ver2Arr[2]}"
	local ver1Flag="${ver1Arr[3]}"
	local ver2Flag="${ver2Arr[3]}"

	#echo "DEBUG:: Version 1: $ver1Num / \"$ver1Flag\""
	#echo "DEBUG:: Version 2: $ver2Num / \"$ver2Flag\""

	local result="$(compareVersionNumbers "$ver1Num" "$ver2Num")"

	if [ "$result" = "=" ]; then
		#echo "DEBUG:: same version number. Checking flag.";
		result="$(compareVersionFlags "$ver1Flag" "$ver2Flag")"
	fi

	echo "$result"
}

#
# Gets the major version of a version number.
# Usage: getMajorVersion "versionNumber"
function getMajorVersion() {
	local versionNum="$1"
	local versionArr=(${versionNum//./ })
	echo "${versionArr[0]}"
}

# Gets the version of an installed package.
# Usage: getInstalledVersion returnVar "infra-jaeger"
# Returns: full name/version string, empty string if not installed.
#
function getInstalledVersion() {
	local returnVar=$1
	local packageName="$2"
	echo "Determining installed version of $packageName"

	local version
	local packageType
	determineSystemPackMan packageType
	if [ "$packageType" == "apt" ]; then
		version="$(apt-cache show "$packageName" | grep "Version:")"
		#echo "DEBUG:: raw version: $version"
		version=($version)
		version="${version[1]}"
	elif [ "$packageType" == "yum" ]; then
		# TODO
		exitProg 2 "yum currently not supported"
	fi

	echo "Done determining installed version: \"$version\""
	eval $returnVar="$version"
}

function getInstalledPackages() {
	local installed=""

	local packageType
	determineSystemPackMan packageType
	if [ "$packageType" == "apt" ]; then
		installed="$(apt-cache pkgnames open+quarter+master-)"
	elif [ "$packageType" == "yum" ]; then
		# TODO
		exitProg 2 "yum currently not supported"
	fi

	# TODO:: packages from other repos

	echo "$installed"
}

#
# Git Interaction Functions
#
#
#

function processReleaseList() {
	echo "Processing release list."

	rm -rf "${RELEASE_VERSIONS_DIR}/*"

	for releaseBase64 in $(jq -r '.[] | @base64' "$RELEASE_LIST_FILE"); do
		processRelease "$(echo "$releaseBase64" | base64 --decode)"
	done

	echo "DONE processing release list."
}

function refreshReleaseList() {
	echo "Refreshing release list."

	cat <<EOT >>"$RELEASE_LIST_FILE_WORKING"
[]
EOT
	#echo "DEBUG:: cur len of releases file: $(stat --printf="%s" "$RELEASE_LIST_FILE_WORKING")";
	local keepCalling=true
	local curGitResponseLen=-1
	local curPage=1
	while [ "$keepCalling" = true ]; do
		#echo "DEBUG:: Hitting: $GIT_RELEASES?per_page=100&page=$curPage"

		local curResponse="$(curl -s -w "%{http_code}" -H "Accept: application/vnd.github+json" "$GIT_RELEASES?per_page=100&page=$curPage")"
		local httpCode=$(tail -n1 <<<"$curResponse")
		local curResponseJson=$(sed '$ d' <<<"$curResponse")

		#echo "DEBUG:: Cur git response: $curResponseJson";

		if [ "$httpCode" != "200" ]; then
			exitProg 1 "Error: Failed to call Git for releases ($httpCode): $curResponseJson"
		fi

		# TODO:: experiment removing data: https://stackoverflow.com/questions/33895076/exclude-column-from-jq-json-output

		curGitResponseLen=$(echo "$curResponseJson" | jq ". | length")
		#cat "$RELEASE_LIST_FILE_WORKING"
		echo "Made call to Git. Cur git response len: \"$curGitResponseLen\""

		jq -c --argjson newReleases "$curResponseJson" '. |= . + $newReleases' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"

		#echo "DEBUG:: cur len of releases file: $(stat --printf="%s" "$RELEASE_LIST_FILE_WORKING")";

		if [ "$curGitResponseLen" -lt 100 ]; then
			keepCalling=false
		fi

		curPage=$((curPage + 1))
	done

	echo "No more releases from Git."

	# TODO:: add setting to enable/disable
	#echo "Removing Pre-releases.";
	#jq -c 'map(select(.prerelease==false))' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	#echo "Done removing Pre-releases."

	# TODO:: remove plugins

	echo "Removing drafts."
	jq -c 'map(select(.draft==false))' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	echo "Done removing drafts."

	echo "Sorting releases."
	jq -c 'sort_by(.published_at) | reverse' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	echo "Done sorting releases."
	#cat "$RELEASE_LIST_FILE_WORKING"

	mv "$RELEASE_LIST_FILE_WORKING" "$RELEASE_LIST_FILE"
	echo "DONE Refreshing release list. $(jq ". | length" "$RELEASE_LIST_FILE") relevant releases returned. Release file $(stat --printf="%s" "$RELEASE_LIST_FILE") bytes in length."
}

#
# Gets the latest release version for the given software.
# Usage: getReleasesFor <software prefix>
#
function getGitReleasesFor() {
	local softwareReleaseToFind="$1"
	local baseStationMajorVersion="$2"

	#echo "getting all releases for $softwareReleaseToFind"

	local releases="$(jq -c "map(select(.name | contains(\"$softwareReleaseToFind\")))" "$RELEASE_LIST_FILE")"

	if [ "$softwareReleaseToFind" == "core-base+station" ] && [ "$baseStationMajorVersion" != "" ]; then
		releases="$(echo "$releases" | jq -c "map(select(.name | contains(\"${softwareReleaseToFind}-$baseStationMajorVersion\")))")"
	fi

	#echo "DEBUG:: got releases for $softwareReleaseToFind: $releases"

	echo "$releases"
}

#
# Gets the latest release version for the given software.
# Usage: getLatestGitReleaseFor <return var> <software prefix>
# Returns Tag json of the latest release
#
function getLatestGitReleaseFor() {
	local softwareReleaseToFind="$1"
	local baseStationMajorVersion="$2"

	releasesFor="$(getGitReleasesFor "$softwareReleaseToFind" "$baseStationMajorVersion")"

	#echo "DEBUG:: releases: $releasesFor"
	#echo "DEBUG:: number of releases: $(echo "$releasesFor" | jq '. | length')"

	echo "$releasesFor" | jq -c '.[0]'
}

#
# Determines if the software tag needs an update
#  TODO:: update to new interface
# Usage: needsUpdated "open+quarter+master-type-name-version[-tag] [base station major version to stick to]"
# Returns "" if not needed, "<tag> <download link>" of version to update to.
#
function needsUpdated() {
	local inputTagVersion="$1"
	local baseStationMajorVersion="$2"

	local curTagArr=(${inputTagVersion//-/ })

	local curMajVersion="$(getMajorVersion "${curTagArr[2]}")"
	local curTag="${curTagArr[1]}-${curTagArr[2]}" # ex: core-base+station
	local curTagVersion="${curTagArr[1]}-${curTagArr[2]}-${curTagArr[3]}"

	#	echo "DEBUG:: cur tag: $curTag"

	output=""
	case "${curTagArr[1]}" in
	"manager" | "infra" | "core") # Prefixes we get from Git
		local latestRelease="$(getLatestGitReleaseFor "$curTag" "$baseStationMajorVersion")"

		#echo "DEBUG:: Latest release: $latestRelease"

		if [ -z "$latestRelease" ]; then
			#echo "No release found for tag prefix $curTag";
			output=""
		else
			latestReleaseTag="$(echo "$latestRelease" | jq -c -r '.name')"
			#echo "DEBUG:: current release: \"$curTagVersion\" Latest release: \"$latestReleaseTag\""
			local compareResult="$(compareVersions "$curTagVersion" "$latestReleaseTag")"
			#echo "DEBUG:: compare result: \"$compareResult\""
			if [[ "$compareResult" == \<* ]]; then
				output="$latestReleaseTag $(getAssetUrlToInstallFromGitRelease "$latestRelease")"
			fi
		fi
		;;
		#TODO:: plugins
	esac
	echo "$output"
}

#
# Gets the json of the asset to install from the release based on which needs to be used to install.
# Usage: selectAssetToInstall "<release json>"
# Returns the json of the asset to install, or empty string if none applicable
#
function getAssetToInstallFromGitRelease() {
	local releaseJson="$1"
	installerFormat=""
	determineSystemPackFileFormat installerFormat

	local matchingAssets="$(echo "$releaseJson" | jq -c ".assets" | jq -c "map(select(.browser_download_url | endswith(\"$installerFormat\")))")"

	local matchingAssetsLen=$(echo "$matchingAssets" | jq ". | length")
	# todo:: check len?

	echo "$matchingAssets" | jq -c '.[0]'
}

function getAssetUrlToInstallFromGitRelease() {
	local releaseJson="$1"
	echo "$(getAssetToInstallFromGitRelease "$releaseJson" | jq -cr '.browser_download_url')"
}

function installFromUrl() {
	local downloadUrl="$1"
	echo "DEBUG:: download url given: $downloadUrl"
	local filename="$(basename "$downloadUrl")"
	local fileLocation="$DOWNLOAD_DIR/$filename"

	if [ -f "$fileLocation" ]; then
		rm "$fileLocation"
	fi

	echo "Downloading file \"$filename\" to \"$fileLocation\" from URL: $downloadUrl"

	local downloadResult="$(curl -s -L -o "$fileLocation" "$downloadUrl")"

	echo "Download result: $downloadResult"
	# TODO:: check filesize is as expected
	# if [  ]; then
	#fi

	# TODO:: this based on system packaging type
	apt install -y "$fileLocation"
	local installResult=$?

	if [ $installResult -ne 0 ]; then
		exitProg 3 "Failed to install package \"$fileLocation\"!"
	fi
}

#
# Gets the appropriate installer from Git, installs it
# Usage: installFromGit "<json of release to install>"
#
#function installFromGit(){
#	local releaseJson="$1"
#
#	local assetToInstall="$(getAssetToInstallFromGitRelease "$releaseJson")"
#	echo "DEBUG:: Installing asset $assetToInstall"
#	# TODO:: download, install
#	local downloadUrl="$(echo "$assetToInstall" | jq -cr '.browser_download_url')"
#	local filename="$(echo "$assetToInstall" | jq -cr '.name')"
#	local fileLocation="$DOWNLOAD_DIR/$filename"
#
#	if [ -f "$fileLocation" ]; then
#		rm "$fileLocation"
#	fi
#
#	echo "Downloading file \"$filename\" to \"$fileLocation\" from URL: $downloadUrl"
#
#	local downloadResult="$(curl -s -L -o "$fileLocation" "$downloadUrl")"
#
#	# TODO:: check filesize is as expected
#	#if [  ]; then
#	#fi
#
#	# TODO:: this based on system packaging type
#	apt install "$fileLocation"
#	local installResult=$?
#
#	if [ $installResult -ne 0 ]; then
#		exitProg 3 "Failed to install package \"$fileLocation\"!"
#	fi
#}

function getGitPackagesForType() {
	local releaseType="$1"
	local releasesOfType="$(getGitReleasesFor "$1-")"

	#echo "DEBUG:: Getting individual packages of type $releaseType: $releasesOfType"
	local infraList=()
	for row in $(echo "$releasesOfType" | jq -r '.[] | @base64'); do
		local curReleaseName="$(echo ${row} | base64 --decode | jq -r '.name')"

		curReleaseName="$(echo "$curReleaseName" | cut -f1,2 -d'-')"
		# shellcheck disable=SC2076
		if [[ ! " ${infraList[*]} " =~ " ${curReleaseName} " ]]; then
			infraList+=("$curReleaseName")
		fi
	done

	#echo "DEBUG:: Got infra pieces: ${infraList[*]}"
	echo "${infraList[*]}"
}

function updateInstallPackagesForType() {
	local releaseType="$1"
	local packages=($(getGitPackagesForType "$1"))
	echo "Packages to install: ${packages[*]}"
	for curPackage in "${packages[@]}"; do
		echo "Installing $curPackage"
		local curRelease="$(getLatestGitReleaseFor $curPackage)"
		local assetUrl="$(getAssetUrlToInstallFromGitRelease "$curRelease")"
		echo "DEBUG:: url gotten for $curPackage: $assetUrl"
		installFromUrl "$assetUrl"
		echo "DONE installing $curPackage"
	done
}

function installUpdateInfra() {
	echo "Installing/Updating latest infrastructure pieces."
	updateInstallPackagesForType "infra"
	echo "DONE Installing/Updating latest infrastructure pieces."
}
function installUpdateBaseStation() {
	echo "Installing/Updating latest infrastructure pieces."
	updateInstallPackagesForType "core"
	echo "DONE Installing/Updating latest infrastructure pieces."
}

function installUpdateInfraBaseStation() {
	installUpdateInfra
	installUpdateBaseStation
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

	local installedPackages=($(getInstalledPackages))
	installedPackages=($(echo "${installedPackages[@]}" | xargs -n1 | sort | xargs))

	pacMan=""
	determineSystemPackMan pacMan

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

function resetDataDialog() {
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

		showDialog --title "Docker cleanup complete!" --msgbox "" 0 $DEFAULT_WIDTH
		;;
	2)
		showDialog --infobox "Cleaning up docker resources. Please wait." 3 $DEFAULT_WIDTH

		# TODO::: check for any other steps?
		docker system prune --volumes

		showDialog --title "Docker cleanup complete!" --msgbox "" 0 $DEFAULT_WIDTH
		;;
	*)
		return
		;;
	esac
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

			showDialog --title "Docker cleanup complete!" --msgbox "" 0 $DEFAULT_WIDTH
			;;
		2)
			showDialog --title "RESET DATA" --yesno "Are you sure? This will erase ALL data used on the system. Configuration will be untouched, but all application data will be gone. It is recommended to backup your data before doing this.\n\nAre you sure?" $DEFAULT_HEIGHT $DEFAULT_WIDTH
			case $? in
			0)
				showDialog --infobox "Resetting all application data. Please wait." 3 $DEFAULT_WIDTH
				systemctl stop open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dmongodb.service

				rm -rf /data/oqm/db/*

				sudo systemctl start open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dmongodb.service

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

##################################################
# Functionality starts here
######################################
###############################
##########################
####################
##############
########
#####
###
#

#
# Check for sudo
#
if [ "$EUID" -ne 0 ]; then
	exitProg 1 "Please run as root! (sudo $0)"
fi

#
# Ensure dirs exist
#

mkdir -p "$META_INFO_DIR"
mkdir -p "$TMP_DIR"
mkdir -p "$DOWNLOAD_DIR"

#
# TODO:: add captain settings file, prepopulate
#

# Update release list. Only call here
refreshReleaseList

#
# Debug section. Nothing should be here
#

#
# Check updatedness of this script
#
curInstalledCapVersion=""
getInstalledVersion curInstalledCapVersion "$SCRIPT_PACKAGE_NAME"
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
getInstalledVersion curInstalledBaseStationVersion "open+quarter+master-core-base+station"
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
getInstalledVersion curInstalledBaseStationVersion "open+quarter+master-core-base+station"
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
