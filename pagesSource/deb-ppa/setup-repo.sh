#!/bin/bash
#Script to setup the Debian OQM repo and install oqm-captain

# get GPG key
curl -s --compressed "https://deployment.openquartermaster.com/deb-ppa/KEY.gpg" | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
#add repo to list
sudo curl -s --compressed -o /etc/apt/sources.list.d/oqm_file.list "https://deployment.openquartermaster.com/deb-ppa/deb_list_file.list"
# update apt and install
sudo apt-get update && sudo apt-get install open+quarter+master-manager-station+captain

clear

echo "Setup of OQM repo and installation of OQM Captain utility complete."
echo ""
echo "Run `sudo oqm-captain` to get started."
echo

