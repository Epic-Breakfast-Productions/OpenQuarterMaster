#!/bin/bash

# 
# This script manages an installation of OpenQuarterMaster
#
# Author: Greg Stewart
# https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster
#

# requires:
#  - dialog
#  - podman
#  - hwinfo
#  - sponge (from moreutils)

SCRIPT_VERSION="1.0.0-DEV"
SCRIPT_VERSION_RELEASE="Manager-Station_Captain-$SCRIPT_VERSION"


# urls
HOME_GIT="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster"
GIT_API_BASE="https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster"
GIT_RELEASES="$GIT_API_BASE/releases"

# files
TMP_DIR="/tmp/oqm"
SHARED_CONFIG_DIR="/etc/oqm"
META_INFO_DIR="$SHARED_CONFIG_DIR/meta"
RELEASE_LIST_FILE="$META_INFO_DIR/releases.json"
RELEASE_LIST_FILE_WORKING="$META_INFO_DIR/releases_unfinished.json"

# Selection
USER_SELECT_FILE="$TMP_DIR/oqm-captain-input"
AUTO_UPDATE_HOST_CRONTAB_FILE="autoUpdateOs"
SELECTION=""
DEFAULT_WIDTH=55
DEFAULT_HEIGHT=15
WIDE_WIDTH=80
# How the user is interacting with this script. Either "ui" or "direct"
INTERACT_MODE="ui";

DIALOG=dialog
#test -n "$DISPLAY" && DIALOG=xdialog

# Software name prefixes
SW_PREFIX_STATION_CAP="Manager-Station_Captain"
SW_PREFIX_INFRA=("Infra-Jaeger" "Infra-MongoDB")

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

# TODO:: take arg to return
function exitProg(){
	
	if [ -f "$USER_SELECT_FILE" ]; then
		rm "$USER_SELECT_FILE";
	fi
	
 	if [ "$1" = "" ]; then
		echo "Exiting."
		clear;
 		exit;
 	else
 		echo "ERROR:: $2";
 		$DIALOG --title "Unrecoverable Error" --msgbox "$2" 30 $WIDE_WIDTH
 		clear
		exit $1
	fi
}

function updateSelection(){
	SELECTION="";
	SELECTION=$(cat $USER_SELECT_FILE);
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
function compareVersionNumbersRec(){
	function sub_ver () {
		local len=${#1}
		temp=${1%%"."*} && indexOf=`echo ${1%%"."*} | echo ${#temp}`
		echo -e "${1:0:indexOf}"
	}
	function cut_dot () {
		local offset=${#1}
		local length=${#2}
		echo -e "${2:((++offset)):length}"
	}
	if [ -z "$2" ] || [ -z "$3" ]; then
		echo "="
	else
		local v1=`echo -e "${2}" | tr -d '[[:space:]]'`
		local v2=`echo -e "${3}" | tr -d '[[:space:]]'`
		local v1_sub=`sub_ver $v1`
		local v2_sub=`sub_ver $v2`
		if (( v1_sub > v2_sub )); then
			echo "> $1"
		elif (( v1_sub < v2_sub )); then
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
function compareVersionNumbers(){
	compareVersionNumbersRec "major" $1 $2
}

#
# Gets the index of an item in an array
# Usage: getIndexInArr "item" "${array[@]}"
#
function getIndexInArr(){
	local needle="$1"
	shift
	local haystack=("$@")
	for i in "${!haystack[@]}"; do
		if [[ "${haystack[$i]}" = "${needle}" ]]; then
			echo "${i}";
			return
		fi
	done
}

#
# Compares release versions
# Usage: compareVersionFlags "type-name-version[-FLAG]" "type-name-version[-FLAG]"
# Returns: "> flag" "< flag" or "="
#
function compareVersionFlags(){
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
function compareVersions(){
	local fullVer1="$1"
	local fullVer2="$2"
	local ver1Arr=(${fullVer1//-/ })
	local ver2Arr=(${fullVer2//-/ })
	
	if [ "${ver1Arr[0]}" != "${ver2Arr[0]}" ] || [ "${ver1Arr[1]}" != "${ver2Arr[1]}" ]; then
		exitProg 66 "ERROR:: Versions given are not comparable \"$fullVer1\", \"$fullVer2\". Please let the developers know of this issue.";
	fi
	
	
	local ver1Num="${ver1Arr[2]}"
	local ver2Num="${ver2Arr[2]}"
	local ver1Flag="${ver1Arr[3]}"
	local ver2Flag="${ver2Arr[3]}"
	
	echo "DEBUG:: Version 1: $ver1Num / \"$ver1Flag\""
	echo "DEBUG:: Version 2: $ver2Num / \"$ver2Flag\""
	
	local result="$(compareVersionNumbers "$ver1Num" "$ver2Num")"

	if [ "$result" = "=" ]; then
		echo "DEBUG:: same version number. Checking flag.";
		result="$(compareVersionFlags "$ver1Flag" "$ver2Flag")"
	fi
	
	echo "$result";
}

#
# Gets the major version of a version number.
# Usage: getMajorVersion "versionNumber"
function getMajorVersion(){
	local versionNum="$1"
	local versionArr=(${versionNum//./ })
	echo "${versionArr[0]}"
}

#
# Git Interaction Functions
#
#
#

function refreshReleaseList(){
	echo "Refreshing release list."
	
	cat <<EOT >> "$RELEASE_LIST_FILE_WORKING"
[]
EOT
	#echo "DEBUG:: cur len of releases file: $(stat --printf="%s" "$RELEASE_LIST_FILE_WORKING")";
	local keepCalling=true
	local curGitResponseLen=-1
	local curPage=1;
	while [ "$keepCalling" = true ] ; do
		#echo "DEBUG:: Hitting: $GIT_RELEASES?per_page=100&page=$curPage"
		
		local curResponse="$(curl -s -w "%{http_code}" -H "Accept: application/vnd.github+json" "$GIT_RELEASES?per_page=100&page=$curPage" )"
		local httpCode=$(tail -n1 <<< "$curResponse")
		local curResponseJson=$(sed '$ d' <<< "$curResponse")
		
		#echo "DEBUG:: Cur git response: $curResponseJson";
		
		if [ "$httpCode" != "200" ]; then
			exitProg 1 "Error: Failed to call Git for releases ($httpCode): $curResponseJson";
		fi
		
		curGitResponseLen=$(echo "$curResponseJson" | jq ". | length")
		#cat "$RELEASE_LIST_FILE_WORKING"
		echo "Made call to Git. Cur git response len: \"$curGitResponseLen\""
		
		jq -c --argjson newReleases "$curResponseJson" '. |= . + $newReleases' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
		
		#echo "DEBUG:: cur len of releases file: $(stat --printf="%s" "$RELEASE_LIST_FILE_WORKING")";
		
		if [ "$curGitResponseLen" -lt 100 ]; then
			keepCalling=false
		fi
		
		curPage=$((curPage+1))
	done
	
	echo "No more releases from Git.";
	
	# TODO:: add setting to enable/disable
	#echo "Removing Pre-releases.";
	#jq -c 'map(select(.prerelease==false))' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	#echo "Done removing Pre-releases."
	
	echo "Removing drafts.";
	jq -c 'map(select(.draft==false))' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	echo "Done removing drafts.";
	
	echo "Sorting releases.";
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
function getReleasesFor(){
	local softwareReleaseToFind="$1"
	
	#echo "getting all releases for $softwareReleaseToFind"
	
	local releases="$(jq -c "map(select(.name | contains(\"$softwareReleaseToFind\")))" "$RELEASE_LIST_FILE")"
	
	#echo "DEBUG:: got releases for $softwareReleaseToFind: $releases"
	
	echo "$releases"
}

#
# Gets the latest release version for the given software.
# Usage: getLatestReleaseFor <return var> <software prefix>
# Returns Tag json of the latest release
#
function getLatestReleaseFor(){
	local softwareReleaseToFind="$1"
		
	releasesFor="$(getReleasesFor "$softwareReleaseToFind")"
	
	#echo "DEBUG:: number of releases: $(echo "$releasesFor" | jq '. | length')"
	#echo "DEBUG:: releases: $releasesFor"
	
	echo "$releasesFor" | jq -c '.[0]'
}


#
# Determines if the software tag needs an update
# Usage: needsUpdated "type-name-version[-tag]"
# Returns "" if not needed, json for release to update to.
#
function needsUpdated() {
	local curTagVersion="$1"
	local curTagArr=(${curTagVersion//-/ })
	
	local curMajVersion="$(getMajorVersion "${curTagArr[2]}")"
	local curTag="${curTagArr[0]}-${curTagArr[1]}"
	
	output=""
	case "${curTagArr[0]}" in
		"Manager" | "Infra") # Prefixes we always want to be latest
			latestRelease="$(getLatestReleaseFor "$curTag")"
			
			#echo "DEBUG:: Latest release: $latestRelease"
			
			if [ -z "$latestRelease" ];then
				#echo "No release found for tag prefix $curTag";
				output=""
			else
				latestReleaseTag="$(echo "$latestRelease" | jq -c -r '.name')"
				#echo "DEBUG:: Latest release: \"$latestReleaseTag\" current release: \"$curTagVersion\""
				local compareResult="$(compareVersions "$curTag" "$latestReleaseTag")"
				if [ "$compareResult" = "<" ]; then
					output="$latestRelease"
				fi
			fi	
			;;
		#TODO:: base station, plugins
	esac
	echo "$output"
}





# 
# User Interaction Functions
# 
# 
# 


function displayBaseOsInfo(){
	# https://medium.com/technology-hits/basic-linux-commands-to-check-hardware-and-system-information-62a4436d40db
	# TODO: format better
	$DIALOG --title "Host OS Info" --msgbox "Ip Address(es): $(hostname -I)\n\n$(cat /etc/os-release)\n\n$(uname -a)\n\nhwinfo:\n$(hwinfo --short)\n\nUSB devices:\n$(lsusb)\n\nDisk usage:\n$(df -H)" 30 $WIDE_WIDTH
}

function displayOQMInfo(){
	# TODO:: parse out most relevant OQM info
	$DIALOG --title "Open QuarterMaster Info" \
	--msgbox "Url: $HOME_GIT" 30 70
}

function getInfo(){
	while true; do
		$DIALOG --title "Info" \
		--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
		1 "Open QuarterMaster" \
		2 "Host/Base OS" \
		2>$USER_SELECT_FILE
		
		updateSelection;
		
		case $SELECTION in
			1) displayOQMInfo
			;;
			2) displayBaseOsInfo
			;;
			*) return
			;;
		esac
	done;
}

function updateBaseSystem(){
	$DIALOG --infobox "Updating Base OS. Please wait." 3 $DEFAULT_WIDTH
	# update base system, based on what distro we are on
	result="";
	resultReturn=0;
	# TODO::: why no work? error during apt, but no err captured
	if [ -n "$(command -v yum)"]; then
		result="$(yum update -y)";
		resultReturn=$?;
	elif [ -n "$(command -v apt)"]; then
		result="$(bash -c 'apt update && apt dist-upgrade -y' 2>&1)";
		resultReturn=$?;
	else
		$DIALOG --title "ERROR: could not update" \
	--msgbox "No recognized command to update with found. Please submit an issue to cover this OS." 30 $DEFAULT_WIDTH
	fi
	
	if [ $resultReturn -ne 0 ]; then
		$DIALOG --title "ERROR: Failed to update" \
	--msgbox "Error updating. Output from command:\n\n${result}" 30 $WIDE_WIDTH
	fi
		
	$DIALOG --title "OS Updates Complete"  --yesno "Restart?" 6 $DEFAULT_WIDTH
	
	case $? in
		# TODO:: reboot not always available?
		0) reboot
			exitProg;
		;;
		*) $DIALOG --title "Updates Complete." --msgbox "" 0 $DEFAULT_WIDTH
		;;
	esac
}

# https://www.cyberciti.biz/faq/how-to-set-up-automatic-updates-for-ubuntu-linux-18-04/
# https://fedoraproject.org/wiki/AutoUpdates
function enableAutoUpdate(){
	echo "TODO";
}
function disableAutoUpdate(){
	echo "TODO";
}

function enableAutomaticOsUpdates(){
	$DIALOG --infobox "Enabling auto OS updates. Please wait." 3 $DEFAULT_WIDTH
	#TODO:: this
	
	$DIALOG --title "Enabled auto OS updates." --msgbox "" 0 $DEFAULT_WIDTH
}

function disableAutomaticOsUpdates(){
	$DIALOG --infobox "Disabling auto OS updates. Please wait." 3 $DEFAULT_WIDTH
	# TODO:: doublecheck
	#crontab -r "$AUTO_UPDATE_HOST_CRONTAB_FILE"
	$DIALOG --title "Disabled auto OS updates." --msgbox "" 0 $DEFAULT_WIDTH
}

function baseOsDialog(){
	while true; do
		$DIALOG --title "Base OS" \
		--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT \
		1 "Update" \
		2 "Enable Automatic OS Updates" \
		3 "Disable Automatic OS Updates" \
		2>$USER_SELECT_FILE
		
		updateSelection;
		
		case $SELECTION in
			1) updateBaseSystem
			;;
			2) enableAutomaticOsUpdates
			;;
			3) disableAutomaticOsUpdates
			;;
			*) return
			;;
		esac
	done;
}

function mainUi(){
	
	while true; do
		$DIALOG --title "Open QuarterMaster Station Captain V${SCRIPT_VERSION}" \
		--menu "Please choose an option:" $DEFAULT_HEIGHT $DEFAULT_WIDTH $DEFAULT_HEIGHT\
		1 "Status" \
		2 "Info" \
		3 "Overall Settings" \
		4 "Manage Base Station" \
		5 "Manage Plugins" \
		6 "Manage Base OS" \
		2>$USER_SELECT_FILE
		
		updateSelection;
		
		case $SELECTION in
			1) getInfo # TODO: status of system
			;;
			2) getInfo
			;;
			3) # TODO: overall settings
			;;
			4) # TODO: Manage base station
			;;
			5) # TODO: manage plugins
			;;
			6) baseOsDialog
			;;
			*) return
			;;
		esac
	done;
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
# Ensure dirs exist
#

mkdir -p "$META_INFO_DIR"
mkdir -p "$TMP_DIR"

#
# TODO:: add captain settings file, prepopulate
#

# Update release list. Only call here 
refreshReleaseList

#
# Check updatedness of this script
#
latestStatCapRelease="$(needsUpdated "$SCRIPT_VERSION_RELEASE")"

# If no releases found
if [ "$latestStatCapRelease" = "" ]; then
	echo "Station Captain up tp date."
else
	echo "Station captain has a new release!";
	# TODO:: update
fi

echo "$(compareVersions "Manager-Station_Captain-1.2.4" "Manager-Station_Captain-1.2.4-DEV")"

#
# Interact with User
#



# TODO:: if get inputs, go to direct mode. If none, ui

#mainUi;

echo "Still working on things! Come back later!"

exitProg;

