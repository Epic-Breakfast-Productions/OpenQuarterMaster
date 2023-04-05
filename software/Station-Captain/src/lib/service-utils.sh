
#
# Gets the index of an item in an array
# Usage: utils_getIndexInArr "item" "${array[@]}"
#
function services-stop(){
	echo "Stopping services";
	systemctl stop open\\x2bquarter\\x2bmaster*
	echo "Done Stopping services";
}
function services-start(){
	echo "Starting services";
	systemctl start open\\x2bquarter\\x2bmaster* --all
	echo "Done Starting services";
}