


function packMan_determineSystemPackMan() {
	local return="$1"
	local result=""
	if [ -n "$(command -v yum)" ]; then
		result="yum"
	elif [ -n "$(command -v apt)" ]; then
		result="apt"
	else
		exitProg 2 "Unable to determine if system uses a supported package manager."
	fi
	eval $return="$result"
}

function packMan_determineSystemPackFileFormat() {
	local return="$1"

	pacMan=""
	packMan_determineSystemPackMan pacMan

	local result=""

	case "$pacMan" in
	"apt")
		result=".deb"
		;;
	"yum")
		result=".rpm"
		;;
	esac

	eval $return="$result"
}



# Gets the version of an installed package.
# Usage: getInstalledVersion returnVar "infra-jaeger"
# Returns: version number string, empty string if not installed.
#
function packMan_getInstalledVersion() {
	local returnVar=$1
	local packageName="$2"
	echo "Determining installed version of $packageName"

	local version
	local packageType
	packMan_determineSystemPackMan packageType
	if [ "$packageType" == "apt" ]; then
		cacheOutput="$(apt-cache show "$packageName")"

		status="$(echo "$cacheOutput" | grep "Status")"
		status=($status)
		status="${status[1]}"

		if [ "$status" == "deinstall" ]; then
			version=""
		else
			version="$(echo "$cacheOutput" | grep "Version:")"
			#echo "DEBUG:: raw version: $version"
			version=($version)
			version="${version[1]}"
		fi
	elif [ "$packageType" == "yum" ]; then
		# TODO
		exitProg 2 "yum currently not supported"
	fi

	echo "Done determining installed version: \"$version\""
	eval $returnVar="$version"
}

function packMan_getInstalledPackages() {
	local installed=""

	local packageType
	packMan_determineSystemPackMan packageType
	if [ "$packageType" == "apt" ]; then
		installed="$(apt-cache pkgnames open+quarter+master-)"
	elif [ "$packageType" == "yum" ]; then
		# TODO
		exitProg 2 "yum currently not supported"
	fi

	# TODO:: packages from other repos

	echo "$installed"
}


#
# Uninstalls all packages that this script manages
#
#
#
# Usage: relUtil_getGitPackagesForType "true|false" "true|false" "true|false"
# Returns
#
function packMan_uninstallAll() {
	local clearData="$1"
	local clearConfigs="$2"
	local includeThis="$3"

	pacMan=""
	packMan_determineSystemPackMan pacMan

	case "$pacMan" in
	"apt")
		apt remove -y --purge open+quarter+master-core-* open+quarter+master-infra-*
		;;
	"yum")
		exitProg 2 "yum not supported"
		;;
	esac

	if [ "$includeThis" = "true" ]; then
		case "$pacMan" in
		"apt")
			apt remove -y --purge open+quarter+master-manager-*
			;;
		"yum")
			exitProg 2 "yum not supported"
			;;
		esac
	fi

	if [ "$clearData" = "true" ]; then
		files-clearData
	fi

	if [ "$clearConfigs" = "true" ]; then
		files-clearData
	fi
}