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
#curl -s --compressed "https://deployment.openquartermaster.com/repos/main/deb/KEY.gpg" | gpg --dearmor | $SUDOTXT tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
wget -q -O - "https://deployment.openquartermaster.com/repos/main/deb/KEY.gpg" | gpg --dearmor | $SUDOTXT tee /etc/apt/trusted.gpg.d/oqm_ppa.gpg >/dev/null
#add repo to list
#curl -s --compressed "https://deployment.openquartermaster.com/repos/main/deb/deb_list_file.list" | $SUDOTXT tee /etc/apt/sources.list.d/oqm_file.list
wget -q -O - "https://deployment.openquartermaster.com/repos/main/deb/deb_list_file.list" | $SUDOTXT tee /etc/apt/sources.list.d/oqm_file.list
# update apt and install
$SUDOTXT apt-get update
if [ $? -ne 0 ]; then
	echo "FAILED to update apt. See above output for information."
	exit 1;
fi

$SUDOTXT apt-get install $AUTO_INSTALL oqm-manager-station+captain
if [ $? -ne 0 ]; then
	echo "FAILED to install Station Captain. See above output for information."
	exit 2;
fi

clear

if [ -z "$AUTO_INSTALL" ]; then
	if [[ -n "$DISPLAY" ]]; then
		read -p "Would you like to install the OQM themes for this OS (can be done later by installing 'oqm-theming')? y/n:" -n 1 -r
		echo;
		if [[ $REPLY =~ ^[Yy]$ ]]; then
			$SUDOTXT apt-get install $AUTO_INSTALL oqm-theming
			if [ $? -ne 0 ]; then
				echo "FAILED to install Station Captain. See above output for information."
				exit 2;
			fi
		fi
	fi
fi

clear

echo "Setup of OQM repo and installation of OQM Captain utility complete."
echo
echo "Run 'sudo oqm-captain' to get started."
echo

