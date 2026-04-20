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
#   - asciidoctor
#
# TODO:: move src to build folder, do replacements there

echo "Script location: ${BASH_SOURCE}"
cd "$(dirname "$0")" || exit

configFile="properties.json"
buildDir="installerBuild"

debDir="ThemingDeb"
rpmDir="ThemingRpm"
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
mkdir -p "$buildDir/$debDir/usr/share/backgrounds/"
mkdir -p "$buildDir/$debDir/usr/share/oqm/"

install -m 755 -D backgrounds/* "$buildDir/$debDir/usr/share/backgrounds/"
install -m 755 -D oqm-gnome-theme.xml "$buildDir/$debDir/usr/share/oqm/"


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
Pre-Depends: $(cat "$configFile" | jq -r '.dependencies.deb')
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


cat <<'EOT' > "$buildDir/$debDir/DEBIAN/postinst"
#!/bin/bash

# GNOME installed, setup
if [ -d "/usr/share/gnome-background-properties/" ]; then
	echo "Detected Gnome install."
	ln -s /usr/share/oqm/oqm-gnome-theme.xml /usr/share/gnome-background-properties/oqm-theme.xml
	dconf update
fi

# TODO:: other desktop environments

EOT
chmod +x "$buildDir/$debDir/DEBIAN/postinst"

dpkg-deb --build --root-owner-group "$buildDir/$debDir" "$outputDir"
if [ $? -ne 0 ]; then
	exit 1;
fi

exit 0;