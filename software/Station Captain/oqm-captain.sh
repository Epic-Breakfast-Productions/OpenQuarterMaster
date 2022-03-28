#!/bin/bash

# 
# This script manages an installation of OpenQuarterMaster
#
# Author: Greg Stewart
# https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster
#

CAPT_VERSION="1.0.0-DEV"
HOME_GIT="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster"
SHARED_CONFIG_DIR="/etc/oqm/"
USER_SELECT_FILE="/tmp/oqm-captain-input"
AUTO_UPDATE_HOST_CRONTAB_FILE="autoUpdateOs"
SELECTION=""
DEFAULT_WIDTH=55
DEFAULT_HEIGHT=15
# How the user is interacting with this script. Either "ui" or "direct"
INTERACT_MODE="ui";

DIALOG=dialog
#test -n "$DISPLAY" && DIALOG=xdialog

# TODO:: take arg to return
function exitProg(){
	clear;
	echo "Exiting."
	exit
}

function updateSelection(){
	SELECTION="";
	SELECTION=$(cat $USER_SELECT_FILE);
}


function displayBaseOsInfo(){
	# TODO:: parse out most relevant os info
	# TODO:: get hardware info
	$DIALOG --title "Host OS Info" \
	--msgbox "$(cat /etc/os-release)" 30 $DEFAULT_WIDTH
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
	# TODO:: update base system, based on what distro we are on
	sleep 2
	
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
		1 "Info" \
		2 "Overall Settings" \
		3 "Manage Base Station" \
		4 "Manage Plugins" \
		5 "Manage Base OS" \
		2>$USER_SELECT_FILE
		
		updateSelection;
		
		case $SELECTION in
			1) getInfo
			;;
			2) # TODO: overall settings
			;;
			3) # TODO: Manage base station
			;;
			4) # TODO: manage plugins
			;;
			5) baseOsDialog
			;;
			*) return
			;;
		esac
	done;
}

# TODO:: if get inputs, go to direct mode. If none, ui
mainUi;

exitProg;

