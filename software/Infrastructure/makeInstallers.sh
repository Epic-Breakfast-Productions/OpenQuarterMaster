#!/bin/bash

#
# Script to make installers for The infrastructure pieces of Open QuarterMaster.
#
# Intended to be run from the dir this resides
#
# Author: Greg Stewart
#
# Requires packages:
#  Ubuntu:
#   - dpkg-dev
#   - rpm
#   - rpmlint
#   - jq
#

mainConfigFile="properties.json"
buildDir="build"

debDir="InfraDeb"

packages=("jaeger" "mongo")

#
# Clean
#

rm -rf "$buildDir"

#
# Setup
#

mkdir "$buildDir"

for curPackage in ${packages[@]}; do
	packageConfigFile="$curPackage/properties.json"
	packageDebDir="$buildDir/$curPackage/$debDir"
	echo "Creating deb installer for $curPackage"
	#
	# Debian build
	#
	
	mkdir -p "$packageDebDir"
	mkdir "$packageDebDir/DEBIAN"
	mkdir -p "$packageDebDir/etc/systemd/system/"
	
	cp "$curPackage/oqm_$curPackage.service" "$packageDebDir/etc/systemd/system/"
	sed -i "s/\${version}/$(cat "$packageConfigFile" | jq -r '.version')/" "$packageDebDir/etc/systemd/system/oqm_$curPackage.service"
	
	# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
	# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
	cat <<EOT >> "$packageDebDir/DEBIAN/control"
Package: $(cat "$packageConfigFile" | jq -r '.packageName')
Version: $(cat "$packageConfigFile" | jq -r '.version')
Maintainer: $(cat "$mainConfigFile" | jq -r '.maintainer.name')
Architecture: all
Description: $(cat "$packageConfigFile" | jq -r '.description')
Homepage: $(cat "$packageConfigFile" | jq -r '.homepage')
Depends: docker
Licence: $(cat "$mainConfigFile" | jq -r '.copyright.licence')
EOT
	# TODO:: enable service after install
#	cat <<EOT >> "$packageDebDir/DEBIAN/copyright"
#Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
#Upstream-Name: Open QuarterMaster Station Captain
#Upstream-Contact: $(cat "$mainConfigFile" | jq -r '.copyright.contact')
#Source: $(cat "$packageConfigFile" | jq -r '.homepage')
#
#Files: *
#Copyright: $(cat "$mainConfigFile" | jq -r '.copyright.copyright')
#License: $(cat "$mainConfigFile" | jq -r '.copyright.licence')
#EOT
	
	dpkg-deb --build "$packageDebDir" "$buildDir"
	
	#	
	# RPM build
	#
done
