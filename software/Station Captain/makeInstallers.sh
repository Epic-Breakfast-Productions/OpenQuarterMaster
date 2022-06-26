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
#

configFile="properties.json"
buildDir="installerBuild"

debDir="StationCaptainDeb"

#
# Clean
#

rm -rf "$buildDir/*"

#
# Setup
#

mkdir "$buildDir"

#
# Debian build
#

mkdir "$buildDir/$debDir"
mkdir "$buildDir/$debDir/DEBIAN"
mkdir "$buildDir/$debDir/din"

cp oqm-captain.sh "$buildDir/$debDir/din/oqm-captain"

# TODO:: reoplace with real info
# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
cat <<EOT >> "$buildDir/$debDir/DEBIAN/control"
Package: Open-QuarterMaster-Station-Captain
Version: 1.0.0-DEV
Maintainer: EBP
Architecture: all
Description: hello world
Homepage: homepage
Depends: bash, hwinfo
EOT

dpkg-deb --build "$buildDir/$debDir" "bin/"




