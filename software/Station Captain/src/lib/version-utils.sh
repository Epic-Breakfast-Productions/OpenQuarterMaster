#
# Version Utility Functions
#
#
#

#
# Compares two version strings. Does not account for flags.
# Usage:  compareVersions "major" "1.2.3" "1.2.3"
# Returns: "> {level}" "< {level}" or "="
# https://unix.stackexchange.com/a/570581/66841
#
function compareVersionNumbersRec() {
	function sub_ver() {
		local len=${#1}
		temp=${1%%"."*} && indexOf=$(echo ${1%%"."*} | echo ${#temp})
		echo -e "${1:0:indexOf}"
	}
	function cut_dot() {
		local offset=${#1}
		local length=${#2}
		echo -e "${2:((++offset)):length}"
	}
	if [ -z "$2" ] || [ -z "$3" ]; then
		echo "="
	else
		local v1=$(echo -e "${2}" | tr -d '[[:space:]]')
		local v2=$(echo -e "${3}" | tr -d '[[:space:]]')
		local v1_sub=$(sub_ver $v1)
		local v2_sub=$(sub_ver $v2)
		if ((v1_sub > v2_sub)); then
			echo "> $1"
		elif ((v1_sub < v2_sub)); then
			echo "< $1"
		else
			local cut1="$(cut_dot $v1_sub $v1)"
			local cut2="$(cut_dot $v2_sub $v2)"
			if [ "$1" = "major" ]; then
				compareVersionNumbersRec "minor" "$cut1" "$cut2"
			elif [ "$1" = "minor" ]; then
				compareVersionNumbersRec "patch" "$cut1" "$cut2"
			else
				compareVersionNumbersRec "" "$cut1" "$cut2"
			fi
		fi
	fi
}

#
# Compares two version strings. Does not account for flags. wrapper for the recursive function.
# Usage:  compareVersions "1.2.3" "1.2.3"
# Returns: "> {level}" "< {level}" or "="
#
function compareVersionNumbers() {
	compareVersionNumbersRec "major" $1 $2
}

#
# Compares release versions
# Usage: compareVersionFlags "type-name-version[-FLAG]" "type-name-version[-FLAG]"
# Returns: "> flag" "< flag" or "="
#
function compareVersionFlags() {
	local flag1="$1"
	local flag2="$2"

	local flagVal1="$(getIndexInArr "$flag1" "${VERSION_FLAG_PRIORITY[@]}")"
	local flagVal2="$(getIndexInArr "$flag2" "${VERSION_FLAG_PRIORITY[@]}")"

	if [ "$flagVal1" -eq "$flagVal2" ]; then
		echo "="
	elif [ "$flagVal1" -gt "$flagVal2" ]; then
		echo "> flag"
	else
		echo "< flag"
	fi
}

#
# Compares release versions
# Usage: compareVersions "type-name-version[-FLAG]" "type-name-version[-FLAG]"
# Returns: "> {level}" "< {level}" or "="
#
function compareVersions() {
	local fullVer1="$1"
	local fullVer2="$2"
	local ver1Arr=(${fullVer1//-/ })
	local ver2Arr=(${fullVer2//-/ })

	if [ "${ver1Arr[0]}" != "${ver2Arr[0]}" ] || [ "${ver1Arr[1]}" != "${ver2Arr[1]}" ]; then
		exitProg 66 "ERROR:: Versions given are not comparable \"$fullVer1\", \"$fullVer2\". Please let the developers know of this issue."
	fi

	local ver1Num="${ver1Arr[2]}"
	local ver2Num="${ver2Arr[2]}"
	local ver1Flag="${ver1Arr[3]}"
	local ver2Flag="${ver2Arr[3]}"

	#echo "DEBUG:: Version 1: $ver1Num / \"$ver1Flag\""
	#echo "DEBUG:: Version 2: $ver2Num / \"$ver2Flag\""

	local result="$(compareVersionNumbers "$ver1Num" "$ver2Num")"

	if [ "$result" = "=" ]; then
		#echo "DEBUG:: same version number. Checking flag.";
		result="$(compareVersionFlags "$ver1Flag" "$ver2Flag")"
	fi

	echo "$result"
}

#
# Gets the major version of a version number.
# Usage: getMajorVersion "versionNumber"
function getMajorVersion() {
	local versionNum="$1"
	local versionArr=(${versionNum//./ })
	echo "${versionArr[0]}"
}