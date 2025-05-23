#!/bin/bash
# This script updates the repositories with all relevant files. Run from the dir this exists in.
# Requires curl, dpkg-dev

# TODO:: rework this script to read from releases, pull out the helm chart, put it in repo dir, run `helm repo index`



GIT_API_BASE="https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster"
GIT_RELEASES="$GIT_API_BASE/releases"
RELEASE_LIST_FILE_CUR="temp.json"

DEB_PPA_DIR="../pagesSource/repos/main/deb"


# cleanup repos
rm  $(ls -1t $DEB_PPA_DIR/*.deb)

# re-populate repos
keepCalling=true
curGitResponseLen=-1
curPage=1
while [ "$keepCalling" = true ]; do
	#echo "DEBUG:: Hitting: $GIT_RELEASES?per_page=100&page=$curPage"

	curResponse="$(curl -s -w "%{http_code}" -H "Accept: application/vnd.github+json" "$GIT_RELEASES?per_page=100&page=$curPage")"
	httpCode=$(tail -n1 <<<"$curResponse")
	curResponseJson=$(sed '$ d' <<<"$curResponse")
	echo "$curResponseJson" > "$RELEASE_LIST_FILE_CUR"

#	echo "DEBUG:: Cur git response: $curResponseJson";

#		echo "$curResponseJson" >> temp.txt

	if [ "$httpCode" != "200" ]; then
		exitProg 1 "Error: Failed to call Git for releases ($httpCode): $curResponseJson"
	fi

	# TODO:: experiment removing data: https://stackoverflow.com/questions/33895076/exclude-column-from-jq-json-output

	curGitResponseLen=$(echo "$curResponseJson" | jq ". | length")
	#cat "$RELEASE_LIST_FILE_WORKING"
	echo "Made call to Git. Cur git response len: \"$curGitResponseLen\""


	for k in $(jq '. | keys | .[]' $RELEASE_LIST_FILE_CUR); do
		curRelease=$(jq -r ".[$k]" $RELEASE_LIST_FILE_CUR);
#		echo "DEBUG:: Cur release: $curRelease";

		for l in $(echo $curRelease | jq '.assets | keys | .[]'); do
			curReleaseAsset=$(echo $curRelease | jq -r ".assets[$l]");
#			echo "DEBUG:: Cur release asset: $curReleaseAsset";
			curAssetFileUrl=$(echo $curReleaseAsset | jq -r ".browser_download_url")
			curAssetFileName=$(echo $curReleaseAsset | jq -r ".name")
			echo "DEBUG:: Cur release asset file: $curAssetFileName -> $curAssetFileUrl";

			if [[ "$curAssetFileName" == oqm-*.deb ]]; then
#				echo "Downloading."
				wget -P "$DEB_PPA_DIR" "$curAssetFileUrl"
			else
				echo "DEBUG:: not a package"
			fi

		done
	done

	if [ "$curGitResponseLen" -lt 100 ]; then
		keepCalling=false
	fi

	curPage=$((curPage + 1))
done

echo "No more releases from Git."

echo "Processing Deb repo files"
pushd "$DEB_PPA_DIR"

dpkg-scanpackages --multiversion . > Packages
gzip -k -f Packages

apt-ftparchive release . > Release
gpg --default-key "OQM Deployment Key" -abs -o - Release > Release.gpg
gpg --default-key "OQM Deployment Key" --clearsign -o - Release > InRelease

git add ./*


popd

