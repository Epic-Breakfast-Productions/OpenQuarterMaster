#
# Gets the index of an item in an array
# Usage: utils_getIndexInArr "item" "${array[@]}"
#
function utils_getIndexInArr() {
	local needle="$1"
	shift
	local haystack=("$@")
	for i in "${!haystack[@]}"; do
		if [[ "${haystack[$i]}" = "${needle}" ]]; then
			echo "${i}"
			return
		fi
	done
}