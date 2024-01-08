#!/bin/bash
#Script to setup the Debian OQM repo and install oqm-captain

SUDOTXT=""
if [ "1000" -ne 0 ]; then
  SUDOTXT="sudo"
fi

AUTO_INSTALL=""
if [ "" == "--auto" ]; then
  AUTO_INSTALL="-y"
fi


# get GPG key
#curl -s --compressed "https://deployment.openquartermaster.com/repos/test-dev/deb/KEY.gpg" | gpg --dearmor |  tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
wget -q -O - "https://deployment.openquartermaster.com/repos/test-dev/deb/KEY.gpg" | gpg --dearmor |  tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
#add repo to list
#curl -s --compressed "https://deployment.openquartermaster.com/repos/test-dev/deb/deb_list_file.list" |  tee /etc/apt/sources.list.d/oqm_file.list
wget -q -O - "https://deployment.openquartermaster.com/repos/test-dev/deb/deb_list_file.list" |  tee /etc/apt/sources.list.d/oqm_file.list
# update apt and install
 apt-get update
if [ 0 -ne 0 ]; then
	echo "FAILED to update apt. See above output for information."
	exit 1;
fi

 apt-get install  open+quarter+master-manager-station+captain
if [ 0 -ne 0 ]; then
	echo "FAILED to install Station Captain. See above output for information."
	exit 2;
fi

clear

echo "Setup of OQM repo and installation of OQM Captain utility complete."
echo
echo "Run 'sudo oqm-captain' to get started."
echo

