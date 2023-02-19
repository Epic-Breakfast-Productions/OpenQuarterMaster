



function exitProg() {
	if [ -f "$USER_SELECT_FILE" ]; then
		rm "$USER_SELECT_FILE"
	fi

	if [ "$1" = "" ]; then
		echo "Exiting."
		if [ "$INTERACT_MODE" = "$INTERACT_MODE_UI" ]; then
			clear -x
		fi
		exit
	else
		echo "ERROR:: $2"

		if [ "$INTERACT_MODE" = "$INTERACT_MODE_UI" ]; then
			showDialog --title "Unrecoverable Error" --msgbox "$2" $TALL_HEIGHT $WIDE_WIDTH
			clear -x
		fi
		exit $1
	fi
}