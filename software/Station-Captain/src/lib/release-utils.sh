
#
# Git Interaction Functions
#
#
#

#function relUtil_processReleaseList() {
#	echo "Processing release list."
#
#	rm -rf "${RELEASE_VERSIONS_DIR}/*"
#
#	for releaseBase64 in $(jq -r '.[] | @base64' "$RELEASE_LIST_FILE"); do
#		processRelease "$(echo "$releaseBase64" | base64 --decode)"
#	done
#
#	echo "DONE processing release list."
#}

function relUtil_refreshReleaseList() {
	echo "Refreshing release list."

	cat <<EOT >>"$RELEASE_LIST_FILE_WORKING"
[]
EOT
	#echo "DEBUG:: cur len of releases file: $(stat --printf="%s" "$RELEASE_LIST_FILE_WORKING")";
	local keepCalling=true
	local curGitResponseLen=-1
	local curPage=1
	while [ "$keepCalling" = true ]; do
		#echo "DEBUG:: Hitting: $GIT_RELEASES?per_page=100&page=$curPage"

		local curResponse="$(curl -s -w "%{http_code}" -H "Accept: application/vnd.github+json" "$GIT_RELEASES?per_page=100&page=$curPage")"
		local httpCode=$(tail -n1 <<<"$curResponse")
		local curResponseJson=$(sed '$ d' <<<"$curResponse")
		echo "$curResponseJson" > "$RELEASE_LIST_FILE_CUR"

#		echo "DEBUG:: Cur git response: $curResponseJson";

#		echo "$curResponseJson" >> temp.txt

		if [ "$httpCode" != "200" ]; then
			exitProg 1 "Error: Failed to call Git for releases ($httpCode): $curResponseJson"
		fi

		# TODO:: experiment removing data: https://stackoverflow.com/questions/33895076/exclude-column-from-jq-json-output

		curGitResponseLen=$(echo "$curResponseJson" | jq ". | length")
		#cat "$RELEASE_LIST_FILE_WORKING"
		echo "Made call to Git. Cur git response len: \"$curGitResponseLen\""

		jq -c --slurpfile newReleases "$RELEASE_LIST_FILE_CUR" '. |= . + $newReleases[0]' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"

		#echo "DEBUG:: cur len of releases file: $(stat --printf="%s" "$RELEASE_LIST_FILE_WORKING")";

		if [ "$curGitResponseLen" -lt 100 ]; then
			keepCalling=false
		fi

		curPage=$((curPage + 1))
	done

	echo "No more releases from Git."

	if [ -s "$RELEASE_LIST_FILE_WORKING" ]; then
		echo "Got releases from Git."
	else
		exitProg 1 "Failed to get releases from GitHub."
	fi


	# TODO:: add setting to enable/disable
	#echo "Removing Pre-releases.";
	#jq -c 'map(select(.prerelease==false))' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	#echo "Done removing Pre-releases."

	# TODO:: remove plugins

	echo "Removing drafts."
	jq -c 'map(select(.draft==false))' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	echo "Done removing drafts."

	echo "Sorting releases."
	jq -c 'sort_by(.published_at) | reverse' "$RELEASE_LIST_FILE_WORKING" | sponge "$RELEASE_LIST_FILE_WORKING"
	echo "Done sorting releases."
	#cat "$RELEASE_LIST_FILE_WORKING"

	mv "$RELEASE_LIST_FILE_WORKING" "$RELEASE_LIST_FILE"
	echo "DONE Refreshing release list. $(jq ". | length" "$RELEASE_LIST_FILE") relevant releases returned. Release file $(stat --printf="%s" "$RELEASE_LIST_FILE") bytes in length."
}

#
# Gets the latest release version for the given software.
# Usage: getReleasesFor <software prefix>
#
function relUtil_getGitReleasesFor() {
	local softwareReleaseToFind="$1"
	local baseStationMajorVersion="$2"

	#echo "getting all releases for $softwareReleaseToFind"

	local releases="$(jq -c "map(select(.name | contains(\"$softwareReleaseToFind\")))" "$RELEASE_LIST_FILE")"

	if [ "$softwareReleaseToFind" == "core-base+station" ] && [ "$baseStationMajorVersion" != "" ]; then
		releases="$(echo "$releases" | jq -c "map(select(.name | contains(\"${softwareReleaseToFind}-$baseStationMajorVersion\")))")"
	fi

	#echo "DEBUG:: got releases for $softwareReleaseToFind: $releases"

	echo "$releases"
}

#
# Gets the latest release version for the given software.
# Usage: relUtil_getLatestGitReleaseFor <release> <base station major version>
# Returns Tag json of the latest release
#
function relUtil_getLatestGitReleaseFor() {
	local softwareReleaseToFind="$1"
	local baseStationMajorVersion="$2"

	releasesFor="$(relUtil_getGitReleasesFor "$softwareReleaseToFind" "$baseStationMajorVersion")"

	#echo "DEBUG:: releases: $releasesFor"
	#echo "DEBUG:: number of releases: $(echo "$releasesFor" | jq '. | length')"

#	if [ "$softwareReleaseToFind" == "core-base+station" ] && [ "$baseStationMajorVersion" != "" ]; then
#		releases="$(echo "$releases" | jq -c "map(select(.name | contains(\"${softwareReleaseToFind}-$baseStationMajorVersion\")))")"
#	fi

	echo "$releasesFor" | jq -c '.[0]'
}

#
# Determines if the software tag needs an update
#  TODO:: update to new interface
# Usage: relUtil_needsUpdated "open+quarter+master-type-name-version[-tag] [base station major version to stick to]"
# Returns "" if not needed, "<tag> <download link>" of version to update to.
#
function relUtil_needsUpdated() {
	local inputTagVersion="$1"
	local baseStationMajorVersion="$2"

	local curTagArr=(${inputTagVersion//-/ })

	local curMajVersion="$(getMajorVersion "${curTagArr[2]}")"
	local curTag="${curTagArr[1]}-${curTagArr[2]}" # ex: core-base+station
	local curTagVersion="${curTagArr[1]}-${curTagArr[2]}-${curTagArr[3]}"

	#	echo "DEBUG:: cur tag: $curTag"

	output=""
	case "${curTagArr[1]}" in
	"manager" | "infra" | "core") # Prefixes we get from Git
		local latestRelease="$(relUtil_getLatestGitReleaseFor "$curTag" "$baseStationMajorVersion")"

		#echo "DEBUG:: Latest release: $latestRelease"

		if [ -z "$latestRelease" ]; then
			#echo "No release found for tag prefix $curTag";
			output=""
		else
			latestReleaseTag="$(echo "$latestRelease" | jq -c -r '.name')"
			#echo "DEBUG:: current release: \"$curTagVersion\" Latest release: \"$latestReleaseTag\""
			local compareResult="$(compareVersions "$curTagVersion" "$latestReleaseTag")"
			#echo "DEBUG:: compare result: \"$compareResult\""
			if [[ "$compareResult" == \<* ]]; then
				output="$latestReleaseTag $(relUtil_getAssetUrlToInstallFromGitRelease "$latestRelease")"
			fi
		fi
		;;
		#TODO:: plugins
	esac
	echo "$output"
}

#
# Gets the json of the asset to install from the release based on which needs to be used to install.
# Usage: selectAssetToInstall "<release json>"
# Returns the json of the asset to install, or empty string if none applicable
#
function relUtil_getAssetToInstallFromGitRelease() {
	local releaseJson="$1"
	installerFormat=""
	packMan_determineSystemPackFileFormat installerFormat

	local matchingAssets="$(echo "$releaseJson" | jq -c ".assets" | jq -c "map(select(.browser_download_url | endswith(\"$installerFormat\")))")"

	local matchingAssetsLen=$(echo "$matchingAssets" | jq ". | length")
	# todo:: check len?
#	echo "Matching assets: $matchingAssets";

	echo "$matchingAssets" | jq -c '.[0]'
}

function relUtil_getAssetUrlToInstallFromGitRelease() {
	local releaseJson="$1"
	echo "$(relUtil_getAssetToInstallFromGitRelease "$releaseJson" | jq -cr '.browser_download_url')"
}

function relUtil_installFromUrl() {
	local downloadUrl="$1"
	echo "DEBUG:: download url given: $downloadUrl"
	local filename="$(basename "$downloadUrl")"
	local fileLocation="$DOWNLOAD_DIR/$filename"

	if [ -f "$fileLocation" ]; then
		rm "$fileLocation"
	fi

	echo "Downloading file \"$filename\" to \"$fileLocation\" from URL: $downloadUrl"

	local downloadResult="$(curl -s -L -o "$fileLocation" "$downloadUrl")"

	echo "Download result: $downloadResult"
	# TODO:: check filesize is as expected
	# if [  ]; then
	#fi

	# TODO:: this based on system packaging type
	apt install -y "$fileLocation"
	local installResult=$?

	if [ $installResult -ne 0 ]; then
		exitProg 3 "Failed to install package \"$fileLocation\"!"
	fi
}

#
# Gets the appropriate installer from Git, installs it
# Usage: installFromGit "<json of release to install>"
#
#function installFromGit(){
#	local releaseJson="$1"
#
#	local assetToInstall="$(relUtil_getAssetToInstallFromGitRelease "$releaseJson")"
#	echo "DEBUG:: Installing asset $assetToInstall"
#	# TODO:: download, install
#	local downloadUrl="$(echo "$assetToInstall" | jq -cr '.browser_download_url')"
#	local filename="$(echo "$assetToInstall" | jq -cr '.name')"
#	local fileLocation="$DOWNLOAD_DIR/$filename"
#
#	if [ -f "$fileLocation" ]; then
#		rm "$fileLocation"
#	fi
#
#	echo "Downloading file \"$filename\" to \"$fileLocation\" from URL: $downloadUrl"
#
#	local downloadResult="$(curl -s -L -o "$fileLocation" "$downloadUrl")"
#
#	# TODO:: check filesize is as expected
#	#if [  ]; then
#	#fi
#
#	# TODO:: this based on system packaging type
#	apt install "$fileLocation"
#	local installResult=$?
#
#	if [ $installResult -ne 0 ]; then
#		exitProg 3 "Failed to install package \"$fileLocation\"!"
#	fi
#}

#
# Gets the releases available for the given type.
# Usage: relUtil_getGitPackagesForType "<type (infra, core)>"
# Returns
#
function relUtil_getGitPackagesForType() {
	local releaseType="$1"
	local releasesOfType="$(relUtil_getGitReleasesFor "$1-")"

	#echo "DEBUG:: Getting individual packages of type $releaseType: $releasesOfType"
	local infraList=()
	for row in $(echo "$releasesOfType" | jq -r '.[] | @base64'); do
		local curReleaseName="$(echo ${row} | base64 --decode | jq -r '.name')"

		curReleaseName="$(echo "$curReleaseName" | cut -f1,2 -d'-')"
		# shellcheck disable=SC2076
		if [[ ! " ${infraList[*]} " =~ " ${curReleaseName} " ]]; then
			infraList+=("$curReleaseName")
		fi
	done

	#echo "DEBUG:: Got infra pieces: ${infraList[*]}"
	echo "${infraList[*]}"
}

function relUtil_updateInstallPackagesForType() {
	local releaseType="$1"
	local packages=($(relUtil_getGitPackagesForType "$1"))
	echo "Packages to install: ${packages[*]}"
	for curPackage in "${packages[@]}"; do
		echo "Installing $curPackage"
		local curRelease="$(relUtil_getLatestGitReleaseFor $curPackage)"
		local assetUrl="$(relUtil_getAssetUrlToInstallFromGitRelease "$curRelease")"
		echo "DEBUG:: url gotten for $curPackage: $assetUrl"
		relUtil_installFromUrl "$assetUrl"
		echo "DONE installing $curPackage"
	done
}

function relUtil_installUpdateInfra() {
	echo "Installing/Updating latest infrastructure pieces."
	relUtil_updateInstallPackagesForType "infra"
	echo "DONE Installing/Updating latest infrastructure pieces."
}
function relUtil_installUpdateBaseStation() {
	echo "Installing/Updating latest infrastructure pieces."
	relUtil_updateInstallPackagesForType "core"
	echo "DONE Installing/Updating latest infrastructure pieces."
}

function relUtil_installUpdateInfraBaseStation() {
	relUtil_installUpdateInfra
	relUtil_installUpdateBaseStation
}
