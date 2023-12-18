#!/bin/bash
# This script updates the repositories with all relevant files. Run from the dir this exists in.
# Requires curl, dpkg-dev

GIT_API_BASE="https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster"
GIT_RELEASES="$GIT_API_BASE/releases"
RELEASE_LIST_FILE_CUR="temp.json"

DEB_PPA_DIR="pagesSource/repos/deb/test-$(git branch --show-current)"



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
cp bin/*.deb "../../$DEB_PPA_DIR";
popd

# Infrastructure
echo -e "\n\n\n\nBuilding Infrastructure."
pushd "../deployment/Single Host/Infrastructure/"
./makeInstallers.sh
if [ $? -ne 0 ]; then
	echo "FAILED to make installers for infrastructure."
	exit 1;
fi
cp build/*.deb "../../$DEB_PPA_DIR";
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

echo "deb [signed-by=/etc/apt/trusted.gpg.d/oqm_ppa.gpg] https://deployment.openquartermaster.com/repos/deb/test-$(git branch --show-current)/ ./" > deb_list_file.list
cp ../main/KEY.gpg .

dpkg-scanpackages --multiversion . > Packages
gzip -k -f Packages

apt-ftparchive release . > Release
gpg --default-key "OQM Deployment Key" -abs -o - Release > Release.gpg
gpg --default-key "OQM Deployment Key" --clearsign -o - Release > InRelease

cat <<EOT >> "setup-repo.sh"
#!/bin/bash
# Script to setup the Debian OQM repo and install oqm-captain

# get GPG key
curl -s --compressed "https://deployment.openquartermaster.com/repos/deb/test-$(git branch --show-current)/KEY.gpg" | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
#add repo to list
curl -s --compressed "https://deployment.openquartermaster.com/repos/deb/test-$(git branch --show-current)/deb_list_file.list" | sudo tee /etc/apt/sources.list.d/oqm_file.list
# update apt and install
sudo apt-get update
if [ $? -ne 0 ]; then
	echo "FAILED to update apt. See above output for information."
	exit 1;
fi

sudo apt-get install open+quarter+master-manager-station+captain
if [ $? -ne 0 ]; then
	echo "FAILED to install Station Captain. See above output for information."
	exit 2;
fi

clear

echo "Setup of TEST OQM repo for branch $(git branch --show-current) and installation of OQM Captain utility complete."
echo
echo "Run 'sudo oqm-captain' to get started."
echo

EOT

popd

