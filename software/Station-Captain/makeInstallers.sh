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
#   - pandoc
#
# TODO:: move src to build folder, do replacements there

echo "Script location: ${BASH_SOURCE}"
cd "$(dirname "$0")" || exit

configFile="properties.json"
buildDir="installerBuild"

debDir="StationCaptainDeb"
rpmDir="StationCaptainRpm"
outputDir="bin/"

sourcesDir="oqm-captain-$(cat "$configFile" | jq -r '.version')"

#
# Clean
#

rm -rf "$sourcesDir"
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
mkdir -p "$buildDir/$debDir/usr/lib/oqm/station-captain"
mkdir -p "$buildDir/$debDir/usr/share/applications"
mkdir -p "$buildDir/$debDir/etc/oqm/static"

cp src/oqm-captain.sh "$buildDir/$debDir/bin/oqm-captain"
cp src/oqm-station-captain-help.txt "$buildDir/$debDir/etc/oqm/static/"
cp src/integration/oqm-icon.svg "$buildDir/$debDir/etc/oqm/static/"
cp src/integration/oqm-sc-icon.svg "$buildDir/$debDir/etc/oqm/static/"
cp src/integration/oqm-sc-guide-icon.svg "$buildDir/$debDir/etc/oqm/static/"
cp src/integration/oqm-captain.desktop "$buildDir/$debDir/usr/share/applications/"
cp src/integration/oqm-captain-user-guide.desktop "$buildDir/$debDir/usr/share/applications/"
cp -r src/lib/* "$buildDir/$debDir/usr/lib/oqm/station-captain/"

pandoc -f gfm docs/User\ Guide.md > "$buildDir/$debDir/etc/oqm/static/stationCaptainUserGuide.html"

sed -i "s/SCRIPT_VERSION='SCRIPT_VERSION'/SCRIPT_VERSION='$(cat "$configFile" | jq -r '.version')'/" "$buildDir/$debDir/bin/oqm-captain"
sed -i 's|LIB_DIR="lib"|LIB_DIR="/usr/lib/oqm/station-captain"|' "$buildDir/$debDir/bin/oqm-captain"

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
mkdir "$buildDir/$rpmDir"
mkdir "$buildDir/$rpmDir/BUILD"
mkdir "$buildDir/$rpmDir/RPMS"
mkdir "$buildDir/$rpmDir/SOURCES"
mkdir "$buildDir/$rpmDir/SPECS"
mkdir "$buildDir/$rpmDir/SRPMS"

# make tar gz

cp -r "src" "$sourcesDir"

sed -i "s/SCRIPT_VERSION='SCRIPT_VERSION'/SCRIPT_VERSION='$(cat "$configFile" | jq -r '.version')'/" "$sourcesDir/oqm-captain.sh"
sed -i 's|LIB_DIR="lib"|LIB_DIR="/usr/lib/oqm/station-captain"|' "$sourcesDir/oqm-captain.sh"

sourcesBundle="$sourcesDir.tar.gz"
tar cvzf "$sourcesBundle" "$sourcesDir"
mv "$sourcesBundle" "$buildDir/$rpmDir/SOURCES"



cat <<EOT >> "$buildDir/$rpmDir/SPECS/oqm-captain.spec"
Name:           oqm-captain
Version:        $(cat "$configFile" | jq -r '.version')
Release:        1%{?dist}
Summary:        A script to manage a Open QuarterMaster install.
BuildArch:      noarch

License:        GPL
Source0:        $sourcesBundle

Requires:       bash, docker

%description
A demo RPM build

%prep
%setup -q

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_libdir}/oqm/station-captain
mkdir -p %{buildroot}/etc/oqm/static
mkdir -p %{buildroot}/usr/share/applications

install -m 755 -D oqm-captain.sh %{buildroot}/%{_bindir}/oqm-captain

%clean
rm -rf ${buildroot}

%files
%{_bindir}/oqm-captain

%changelog

EOT

echo "$buildDir/$rpmDir/SPECS/"

rpmlint "$buildDir/$rpmDir/SPECS/oqm-captain.spec"

rpmbuild --define "_topdir `pwd`/$buildDir/$rpmDir/" -bb "$buildDir/$rpmDir/SPECS/oqm-captain.spec"

cp -a "$buildDir/$rpmDir/RPMS/noarch/." "$outputDir"
