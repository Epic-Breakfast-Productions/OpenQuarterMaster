#!/bin/bash
# This script updates the repositories with all relevant files. Run from the dir this exists in.
# Requires curl, dpkg-dev

GIT_API_BASE="https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster"
GIT_RELEASES="$GIT_API_BASE/releases"
RELEASE_LIST_FILE_CUR="temp.json"

REPO_DIR="pagesSource/repos/test-$(git branch --show-current)"
DEB_PPA_DIR="$REPO_DIR/deb"

echo $DEB_PPA_DIR

# Prepare Deb repo
mkdir -p "../$DEB_PPA_DIR"
rm -rf "../$DEB_PPA_DIR/"*
rm -rf "../$DEB_PPA_DIR/".*

# TODO:: foreach software packaged, make installers, copy into new repo

# Station Captain
echo -e "\n\n\n\nBuilding Station Captain."
pushd "../deployment/Single Host/Station-Captain/"
./makeInstallers.sh
if [ $? -ne 0 ]; then
	echo "FAILED to make installers for station captain."
	exit 1;
fi
cp bin/*.deb "../../../$DEB_PPA_DIR";
popd

# Infrastructure
echo -e "\n\n\n\nBuilding Infrastructure."
pushd "../deployment/Single Host/Infrastructure/"
./makeInstallers.sh
if [ $? -ne 0 ]; then
	echo "FAILED to make installers for infrastructure."
	exit 1;
fi
cp build/*.deb "../../../$DEB_PPA_DIR";
popd

# Base Station
echo -e "\n\n\n\nBuilding Base Station."
pushd "../software/open-qm-base-station/"
./makeInstallers.sh
if [ $? -ne 0 ]; then
	echo "FAILED to make installers for Base Station."
	exit 1;
fi
cp build/installers/*.deb "../../$DEB_PPA_DIR";
popd


echo -e "\n\n\n\nProduced debian installers: "
ls "../$DEB_PPA_DIR"
echo

# Setup deb repo
echo "Processing Deb repo files"
pushd "../$DEB_PPA_DIR"

echo "deb [signed-by=/etc/apt/trusted.gpg.d/oqm_ppa.gpg] https://deployment.openquartermaster.com/repos/test-$(git branch --show-current)/deb/ ./" > deb_list_file.list
cp ../../main/deb/KEY.gpg .

dpkg-scanpackages --multiversion . > Packages
gzip -k -f Packages

apt-ftparchive release . > Release
gpg --default-key "OQM Deployment Key" -abs -o - Release > Release.gpg
gpg --default-key "OQM Deployment Key" --clearsign -o - Release > InRelease

cat <<EOT >> "setup-repo.sh"
#!/bin/bash
#Script to setup the Debian OQM repo and install oqm-captain

SUDOTXT=""
if [ "$EUID" -ne 0 ]; then
  SUDOTXT="sudo"
fi

AUTO_INSTALL=""
if [ "$1" == "--auto" ]; then
  AUTO_INSTALL="-y"
fi


# get GPG key
#curl -s --compressed "https://deployment.openquartermaster.com/repos/test-$(git branch --show-current)/deb/KEY.gpg" | gpg --dearmor | $SUDOTXT tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
wget -q -O - "https://deployment.openquartermaster.com/repos/test-$(git branch --show-current)/deb/KEY.gpg" | gpg --dearmor | $SUDOTXT tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
#add repo to list
#curl -s --compressed "https://deployment.openquartermaster.com/repos/test-$(git branch --show-current)/deb/deb_list_file.list" | $SUDOTXT tee /etc/apt/sources.list.d/oqm_file.list
wget -q -O - "https://deployment.openquartermaster.com/repos/test-$(git branch --show-current)/deb/deb_list_file.list" | $SUDOTXT tee /etc/apt/sources.list.d/oqm_file.list
# update apt and install
$SUDOTXT apt-get update
if [ $? -ne 0 ]; then
	echo "FAILED to update apt. See above output for information."
	exit 1;
fi

$SUDOTXT apt-get install $AUTO_INSTALL open+quarter+master-manager-station+captain
if [ $? -ne 0 ]; then
	echo "FAILED to install Station Captain. See above output for information."
	exit 2;
fi

clear

echo "Setup of OQM repo and installation of OQM Captain utility complete."
echo
echo "Run 'sudo oqm-captain' to get started."
echo

EOT

popd

git add "../$REPO_DIR/*"
