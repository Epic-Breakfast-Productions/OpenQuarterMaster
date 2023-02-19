
function showDialog() {
	dialog --backtitle "$SCRIPT_TITLE" --hfile "oqm-station-captain-help.txt" "$@"
}

function updateSelection() {
	SELECTION=""
	SELECTION=$(cat $USER_SELECT_FILE)
}