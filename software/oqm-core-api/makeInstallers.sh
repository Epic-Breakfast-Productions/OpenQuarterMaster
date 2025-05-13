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

debDir="oqmCoreAPiDeb"

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
mkdir -p "$buildDir/$debDir/etc/oqm/static/media/core/api/"
mkdir -p "$buildDir/$debDir/etc/oqm/serviceConfig/core/api/"
mkdir -p "$buildDir/$debDir/etc/oqm/config/configs/"
mkdir -p "$buildDir/$debDir/etc/oqm/proxyConfig.d/"
mkdir -p "$buildDir/$debDir/usr/share/applications"

install -m 755 -D "$srcDir/core-api.svg" "$buildDir/$debDir/etc/oqm/static/media/core/api/core-api.svg"
install -m 755 -D "$srcDir/uiEntry.json" "$buildDir/$debDir/etc/oqm/ui.d/oqm-core-api.json"
install -m 755 -D "$srcDir/core-api-config.list" "$buildDir/$debDir/etc/oqm/serviceConfig/core/api/"
install -m 755 -D "$srcDir/20-coreApi.json" "$buildDir/$debDir/etc/oqm/config/configs/"
install -m 755 -D "$srcDir/core-api-proxy-config.json" "$buildDir/$debDir/etc/oqm/proxyConfig.d/"

serviceFile="oqm-core-api.service"
serviceFileEscaped="$serviceFile" # "$(systemd-escape "$serviceFile")"

cp "$srcDir/$serviceFile" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"
sed -i "s/\${version}/$(./gradlew -q printVersion)/" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"

# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
cat <<EOT >> "$buildDir/$debDir/DEBIAN/control"
Package: $(cat "$configFile" | jq -r '.packageName')
Version: $(./gradlew -q printVersion)
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
Upstream-Name: Open QuarterMaster Core API
Upstream-Contact: $(cat "$configFile" | jq -r '.copyright.contact')
Source: $(cat "$configFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$configFile" | jq -r '.copyright.copyright')
License: $(cat "$configFile" | jq -r '.copyright.licence')
EOT


cat <<EOT >> "$buildDir/$debDir/DEBIAN/preinst"
#!/bin/bash

mkdir -p /etc/oqm/serviceConfig/core/api/files/

if [ ! -f "/etc/oqm/serviceConfig/core/api/user-config.list" ]; then
	cat <<EOF >> "/etc/oqm/serviceConfig/core/api/user-config.list"
# Add your own config here.
# Configuration here will override those in core-api-config.list
# Reference: https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/software/oqm-core-api/docs/BuildingAndDeployment.adoc

# quarkus.log.level=DEBUG

EOF
fi
EOT
chmod +x "$buildDir/$debDir/DEBIAN/preinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postinst"
#!/bin/bash

systemctl daemon-reload
# restart proxy after we add config
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
if [[ "$(docker images -q oqm-core-api 2> /dev/null)" != "" ]]; then
        docker rmi oqm-core-api
        echo "Removed docker image."
else
        echo "Docker image was already gone."
fi
if [ $( docker ps -a | grep oqm-core-api | wc -l ) -gt 0 ]; then
        docker rm oqm-core-api
        echo "Removed docker container."
else
        echo "Docker container was already gone."
fi

EOT
chmod +x "$buildDir/$debDir/DEBIAN/postrm"

dpkg-deb --build "$buildDir/$debDir" "$buildDir"



#
# RPM build
#


