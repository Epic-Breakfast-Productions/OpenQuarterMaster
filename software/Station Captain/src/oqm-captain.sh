#!/bin/bash
# This script manages an installation of Open QuarterMaster on a single host
#
# Author: Greg Stewart
# https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster
#
# requires:
#  - dialog
#  - docker
#  - hwinfo
#  - sponge (from moreutils)
#  - curl
#  - jq
SCRIPT_VERSION='SCRIPT_VERSION'
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
RELEASE_LIST_FILE_CUR="$META_INFO_DIR/releases_cur.json"
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
INTERACT_MODE="$INTERACT_MODE_DIRECT"

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
# Load in utilities
######################################
###############################
##########################
####################
##############
########
#####
###
#

LIB_DIR="lib"

source "$LIB_DIR/prog-flow.sh"
if [ $? -ne 0 ]; then echo "ERROR: Unable to source lib prog-flow"; exit 255; fi;
source "$LIB_DIR/package-management.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib package-management"; fi;
source "$LIB_DIR/utils.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib utils"; fi;
source "$LIB_DIR/version-utils.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib version-utils"; fi;
source "$LIB_DIR/release-utils.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib release-utils"; fi;
source "$LIB_DIR/user-interaction.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib user-interaction"; fi;



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

while getopts 'h' opt; do
	case "$opt" in
		h)
			echo "Usage: $(basename $0)"
			exitProg;
		;;
	esac
done

INTERACT_MODE="$INTERACT_MODE_UI"

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
