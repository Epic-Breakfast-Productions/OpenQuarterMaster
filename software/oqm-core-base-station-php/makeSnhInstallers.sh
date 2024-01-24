#!/bin/bash
#
# Script to make installers for OQM- Base Station.
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
# TODO:: Figure out how logs work
cd "$(dirname "$0")" || exit
srcDir="installerSrc"
configFile="$srcDir/installerProperties.json"
buildDir="build/installers"

debDir="DepotDeb"

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
mkdir -p "$buildDir/$debDir/etc/oqm/config/configs/"
mkdir -p "$buildDir/$debDir/etc/oqm/proxyConfig.d/"
mkdir -p "$buildDir/$debDir/etc/oqm/kcClients/"
mkdir -p "$buildDir/$debDir/usr/share/applications"

install -m 755 -D "$srcDir/20-core-depot.json" "$buildDir/$debDir/etc/oqm/config/configs/"
install -m 755 -D "$srcDir/oqm-depot.desktop" "$buildDir/$debDir/usr/share/applications/"
#install -m 755 -D "$srcDir/core-baseStation-proxy-config.json" "$buildDir/$debDir/etc/oqm/proxyConfig.d/"
#install -m 755 -D "$srcDir/baseStationClient.json" "$buildDir/$debDir/etc/oqm/kcClients/"

serviceFile="oqm-core-depot.service"
serviceFileEscaped="$serviceFile" #"$(systemd-escape "$serviceFile")"

cp "$srcDir/$serviceFile" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"
sed -i "s/\${version}/$(jq -r '.version' webroot/composer.json)/" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"

# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
cat <<EOT >> "$buildDir/$debDir/DEBIAN/control"
Package: $(cat "$configFile" | jq -r '.packageName')
Version: $(jq -r '.version' webroot/composer.json)
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
Upstream-Name: Open QuarterMaster Base Station
Upstream-Contact: $(cat "$configFile" | jq -r '.copyright.contact')
Source: $(cat "$configFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$configFile" | jq -r '.copyright.copyright')
License: $(cat "$configFile" | jq -r '.copyright.licence')
EOT


cat <<EOT >> "$buildDir/$debDir/DEBIAN/preinst"
#!/bin/bash

EOT
chmod +x "$buildDir/$debDir/DEBIAN/preinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postinst"
#!/bin/bash

systemctl daemon-reload
# restart proxy after we add config
#systemctl restart "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service"
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
#systemctl restart "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service"

EOT
chmod +x "$buildDir/$debDir/DEBIAN/postrm"

dpkg-deb --build "$buildDir/$debDir" "$buildDir"


#
# RPM build
#


