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
STATION_CAPT_GIT="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/tree/main/software/Station%20Captain"
HOME_PLUGIN_REPO="https://raw.githubusercontent.com/Epic-Breakfast-Productions/OpenQuarterMaster/main/software/plugins/plugin-repo.json"
GIT_API_BASE="https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster"
GIT_RELEASES="$GIT_API_BASE/releases"

# files
TMP_DIR="/tmp/oqm"
DOWNLOAD_DIR="$TMP_DIR/download"
DATA_DIR="/data/oqm"
SHARED_CONFIG_DIR="/etc/oqm"
META_INFO_DIR="$SHARED_CONFIG_DIR/meta"
CONFIG_VALUES_DIR="$SHARED_CONFIG_DIR/config/configs"
SERVICE_CONFIG_DIR="$SHARED_CONFIG_DIR/serviceConfig"
RELEASE_LIST_FILE="$META_INFO_DIR/releases.json"
RELEASE_LIST_FILE_WORKING="$META_INFO_DIR/releases_unfinished.json"
RELEASE_LIST_FILE_CUR="$META_INFO_DIR/releases_cur.json"
RELEASE_VERSIONS_DIR="$META_INFO_DIR/versions"
RELEASE_INFRA_VERSIONS="$RELEASE_VERSIONS_DIR/infra.json"
RELEASE_MNGR_VERSIONS="$RELEASE_VERSIONS_DIR/manager.json"
BACKUP_SCRIPTS_LOC="$SHARED_CONFIG_DIR/backup/scripts"

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

HELPTEXT="$SCRIPT_TITLE

Usage:
	$(basename $0) [-h]

	No arguments assumes an interface is desired, and runs with an interactive console-based ui.

	General:

		-h
		--help
			Displays this help text

		--sys-info
			TODO
			Gets the system's information collected in one place.

		--install-info
			TODO
			Gets installation information such as the major version of base station used, and what is installed.

		-v
		--version
			Displays this script's version

	Installation management:

		--install
			TODO
			Installs the core components; Infrastructure components and the base station.

		--install -p <plugin>
			TODO
			installs a plugin by name

		--update -a
			TODO
			Finds and installs all available updates

		--uninstall -a [-c]
			TODO
			Uninstalls everything this script manages. -c for removing configuration as well.

		--uninstall <plugin>
			TODO
			Uninstalls a particular plugin.

	Data Management:

		--reset-data
			TODO
			Purges ALL data stored by the system. Do so with care, recommend backing up data first.

	System Management:

		--image-clean
			TODO
			Cleans container images. Recommend doing after updates.

		--os-auto-update [-e|-d]
			TODO
			Enables or disables OS automated updates. -e to endable, -d to disable


Notes:
	- Due to the nature of what the script manages/does, root access is required. Run as either the 'root' user or with 'sudo'
	- Further documentation available here: $STATION_CAPT_GIT
"




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
source "$LIB_DIR/file-utils.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib file-utils"; fi;
source "$LIB_DIR/service-utils.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib service-utils"; fi;
source "$LIB_DIR/snapshot_restore.sh"
if [ $? -ne 0 ]; then exitProg 255 "Unable to source lib snapshot_restore"; fi;



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

if [ "$#" -eq 0 ]; then
	INTERACT_MODE="$INTERACT_MODE_UI"
	ui.doInteraction
fi

ARGS_SHORT="v,h"
ARGS_LONG="version,help"

OPTS=$(getopt -a -n oqm-captain --options $ARGS_SHORT --longoptions $ARGS_LONG -- "$@")

VALID_ARGUMENTS=$# # Returns the count of arguments that are in short or long options
if [ "$VALID_ARGUMENTS" -eq 0 ]; then
	echo "$HELPTEXT";
	exitProg 1 "Bad inputs given.";
fi

eval set -- "$OPTS"
while :
do
	case "$1" in
		-v | --version )
			echo "$SCRIPT_TITLE";
			exitProg;
		;;
		-h | --help)
			echo "$HELPTEXT";
			exitProg;
		;;
		--)
			shift;
			break
		;;
	esac
done
