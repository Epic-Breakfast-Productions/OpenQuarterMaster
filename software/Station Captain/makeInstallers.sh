#!/bin/bash

#
# Script to make installers for Station Master.
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

echo "Script location: ${BASH_SOURCE}"
cd "$(dirname "$0")" || exit

configFile="properties.json"
buildDir="installerBuild"

debDir="StationCaptainDeb"
outputDir="bin/"

#
# Clean
#

rm -rf "$buildDir"
rm -rf "$outputDir"

#
# Setup
#

mkdir "$buildDir"

#
# Debian build
#

mkdir "$outputDir"
mkdir "$buildDir/$debDir"
mkdir "$buildDir/$debDir/DEBIAN"
mkdir "$buildDir/$debDir/bin"
mkdir -p "$buildDir/$debDir/lib/oqm/station-captain"
mkdir -p "$buildDir/$debDir/etc/oqm/static"
mkdir -p "$buildDir/$debDir/usr/share/applications"

cp src/oqm-captain.sh "$buildDir/$debDir/bin/oqm-captain"
cp src/oqm-station-captain-help.txt "$buildDir/$debDir/etc/oqm/static/"
cp src/oqm-icon.svg "$buildDir/$debDir/etc/oqm/static/"
cp src/oqm-captain.desktop "$buildDir/$debDir/usr/share/applications/"
cp -r src/lib/* "$buildDir/$debDir/lib/oqm/station-captain/"

sed -i "s/SCRIPT_VERSION='SCRIPT_VERSION'/SCRIPT_VERSION='$(cat "$configFile" | jq -r '.version')'/" "$buildDir/$debDir/bin/oqm-captain"
sed -i 's|LIB_DIR="lib"|LIB_DIR="/lib/oqm/station-captain"|' "$buildDir/$debDir/bin/oqm-captain"

# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
cat <<EOT >> "$buildDir/$debDir/DEBIAN/control"
Package: $(cat "$configFile" | jq -r '.packageName')
Version: $(cat "$configFile" | jq -r '.version')
Section: Open QuarterMaster
Maintainer: $(cat "$configFile" | jq -r '.maintainer.name')
Architecture: all
Description: $(cat "$configFile" | jq -r '.description')
Homepage: $(cat "$configFile" | jq -r '.homepage')
Depends: $(cat "$configFile" | jq -r '.dependencies.deb')
Licence: $(cat "$configFile" | jq -r '.copyright.licence')
EOT

cat <<EOT >> "$buildDir/$debDir/DEBIAN/copyright"
Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: Open QuarterMaster Station Captain
Upstream-Contact: $(cat "$configFile" | jq -r '.copyright.contact')
Source: $(cat "$configFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$configFile" | jq -r '.copyright.copyright')
License: $(cat "$configFile" | jq -r '.copyright.licence')
EOT

dpkg-deb --build "$buildDir/$debDir" "$outputDir"


#
# RPM build
#


