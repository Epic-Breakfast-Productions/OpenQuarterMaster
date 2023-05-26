#
# Gets the index of an item in an array
# Usage: utils_getIndexInArr "item" "${array[@]}"
#
function files_clearData(){
	rm -rf "$DATA_DIR"
}
function files_clearConfig(){
	# rm -rf "$CONFIG_VALUES_DIR"
	rm -rf "$SERICE_CONFIG_DIR"
}