#!/bin/bash
# Script to setup the Debian OQM repo and install oqm-captain

# get GPG key
curl -s --compressed "https://deployment.openquartermaster.com/repos/deb/test-dev/KEY.gpg" | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
#add repo to list
curl -s --compressed "https://deployment.openquartermaster.com/repos/deb/test-dev/deb_list_file.list" | sudo tee /etc/apt/sources.list.d/oqm_file.list
# update apt and install
sudo apt-get update
if [ 0 -ne 0 ]; then
	echo "FAILED to update apt. See above output for information."
	exit 1;
fi

sudo apt-get install open+quarter+master-manager-station+captain
if [ 0 -ne 0 ]; then
	echo "FAILED to install Station Captain. See above output for information."
	exit 2;
fi

clear

echo "Setup of TEST OQM repo for branch dev and installation of OQM Captain utility complete."
echo
echo "Run 'sudo oqm-captain' to get started."
echo
