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


CAPT_VERSION="1.0.0-DEV"
HOME_GIT="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster"
SHARED_CONFIG_DIR="/etc/oqm/"
USER_SELECT_FILE="/tmp/oqm-captain-input"
AUTO_UPDATE_HOST_CRONTAB_FILE="autoUpdateOs"
SELECTION=""
DEFAULT_WIDTH=55
DEFAULT_HEIGHT=15
WIDE_WIDTH=80
# How the user is interacting with this script. Either "ui" or "direct"
INTERACT_MODE="ui";

DIALOG=dialog
#test -n "$DISPLAY" && DIALOG=xdialog

# TODO:: take arg to return
function exitProg(){
	clear;
	echo "Exiting."
	rm "$USER_SELECT_FILE";
	exit
}

function updateSelection(){
	SELECTION="";
	SELECTION=$(cat $USER_SELECT_FILE);
}


function displayBaseOsInfo(){
	# https://medium.com/technology-hits/basic-linux-commands-to-check-hardware-and-system-information-62a4436d40db
	# TODO: format better
	$DIALOG --title "Host OS Info" \
	--msgbox "Ip Address(es): $(hostname -I)\n\n$(cat /etc/os-release)\n\n$(uname -a)\n\nhwinfo:\n$(hwinfo --short)\n\nUSB devices:\n$(lsusb)\n\nDisk usage:\n$(df -H)" 30 $WIDE_WIDTH
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
		$DIALOG --title "Open QuarterMaster Station Captain V${CAPT_VERSION}" \
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

# TODO:: if get inputs, go to direct mode. If none, ui
mainUi;

exitProg;

