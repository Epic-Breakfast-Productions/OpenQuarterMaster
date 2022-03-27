#!/bin/bash
CAPT_VERSION="1.0.0-DEV"
HOME_GIT="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster"
SHARED_CONFIG_DIR="/etc/oqm/"
USER_SELECT_FILE="/tmp/oqm-captain-input"
SELECTION=""
# How the user is interacting with this script. Either "ui" or "direct"
INTERACT_MODE="ui";

DIALOG=dialog
#test -n "$DISPLAY" && DIALOG=xdialog

function updateSelection(){
	SELECTION=$(cat $USER_SELECT_FILE);
}

function getInfo(){
	# TODO:: get info about the running system
	echo;
}

# TODO:: if get inputs, go to direct mode. If none, ui

$DIALOG --title "Open QuarterMaster Station Captain V${CAPT_VERSION}" \
--menu "Please choose an option:" 15 55 5 \
1 "Info" \
2 "Delete a record from DB" \
3 "Exit from this menu" 2>$USER_SELECT_FILE


updateSelection;
dialog --infobox "Selected Option $SELECTION" 3 34 ; sleep 5


echo;

