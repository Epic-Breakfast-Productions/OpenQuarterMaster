#!/bin/bash
#
# Script to make installers for OQM- Characteristics.
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

cd "$(dirname "$0")" || exit
srcDir="installerSrc"
configFile="$srcDir/installerProperties.json"
buildDir="build/installers"

debDir="CharacteristicsDeb"

#
# Clean
#

rm -rf "$buildDir"
#
# Setup
#

mkdir -p "$buildDir"

#
# Debian build
#

mkdir "$buildDir/$debDir"
mkdir "$buildDir/$debDir/DEBIAN"
mkdir -p "$buildDir/$debDir/etc/systemd/system/"
mkdir -p "$buildDir/$debDir/etc/oqm/serviceConfig/core/characteristics/runBy/"
mkdir -p "$buildDir/$debDir/etc/oqm/config/configs/"

install -m 755 -D "$srcDir/core-characteristics-config.list" "$buildDir/$debDir/etc/oqm/serviceConfig/core/characteristics/"
install -m 755 -D "$srcDir/20-core-characteristics.json" "$buildDir/$debDir/etc/oqm/config/configs/"

serviceFile="oqm-core-characteristics.service"
serviceFileEscaped="$serviceFile" # "$(systemd-escape "$serviceFile")"

cp "$srcDir/$serviceFile" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"
sed -i "s/\${version}/$(cat "$configFile" | jq -r '.version')/" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"


cat <<EOT >> "$buildDir/$debDir/etc/oqm/serviceConfig/core/characteristics/runBy/README.md"
# Run By Images

The files in this directory are presented to the Characteristics service.

Place images in her to be made available for `runBy` image config.

EOT

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
Recommends: $(cat "$configFile" | jq -r '.dependencies.debRec')
Licence: $(cat "$configFile" | jq -r '.copyright.licence')
EOT

cat <<EOT >> "$buildDir/$debDir/DEBIAN/copyright"
Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: Open QuarterMaster Characteristics
Upstream-Contact: $(cat "$configFile" | jq -r '.copyright.contact')
Source: $(cat "$configFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$configFile" | jq -r '.copyright.copyright')
License: $(cat "$configFile" | jq -r '.copyright.licence')
EOT


cat <<EOT >> "$buildDir/$debDir/DEBIAN/preinst"
#!/bin/bash

mkdir -p /etc/oqm/serviceConfig/core/characteristics/

if [ ! -f "/etc/oqm/serviceConfig/core/characteristics/user-config.list" ]; then
	cat <<EOF >> "/etc/oqm/serviceConfig/core/characteristics/user-config.list"
# Add your own config here.
# Configuration here will override those in characteristics-config.list
# Reference: https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/software/oqm-characteristics


EOF
fi
EOT
chmod +x "$buildDir/$debDir/DEBIAN/preinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postinst"
#!/bin/bash

systemctl daemon-reload
systemctl enable "$serviceFileEscaped"
systemctl start "$serviceFileEscaped"
EOT
chmod +x "$buildDir/$debDir/DEBIAN/postinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/prerm"
#!/bin/bash

systemctl disable "$serviceFileEscaped"
systemctl stop "$serviceFileEscaped"
EOT
chmod +x "$buildDir/$debDir/DEBIAN/prerm"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postrm"
#!/bin/bash

systemctl daemon-reload
# Remove docker image
if [[ "$(docker images -q oqm-core-characteristics 2> /dev/null)" != "" ]]; then
        docker rmi oqm-core-characteristics
        echo "Removed docker image."
else
        echo "Docker image was already gone."
fi
if [ $( docker ps -a | grep oqm-core-characteristics | wc -l ) -gt 0 ]; then
        docker rm oqm-core-characteristics
        echo "Removed docker container."
else
        echo "Docker container was already gone."
fi

EOT
chmod +x "$buildDir/$debDir/DEBIAN/postrm"

dpkg-deb --build --root-owner-group "$buildDir/$debDir" "$buildDir"



#
# RPM build
#


