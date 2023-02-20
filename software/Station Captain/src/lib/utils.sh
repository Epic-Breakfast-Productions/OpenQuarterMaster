#
# Gets the index of an item in an array
# Usage: getIndexInArr "item" "${array[@]}"
#
function getIndexInArr() {
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