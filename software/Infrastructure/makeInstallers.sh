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

packages=("jaeger" "mongo" "prometheus")

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
	sed -i "s/\${version}/$(jq -r '.version' "$packageConfigFile")/" "$packageDebDir/etc/systemd/system/oqm_$curPackage.service"

	# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
	# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
	cat <<EOT >> "$packageDebDir/DEBIAN/control"
Package: $(cat "$packageConfigFile" | jq -r '.packageName')
Version: $(cat "$packageConfigFile" | jq -r '.version')
Section: Open QuarterMaster
Maintainer: $(cat "$mainConfigFile" | jq -r '.maintainer.name')
Developer: EBP
Architecture: all
Description: $(cat "$packageConfigFile" | jq -r '.description')
Homepage: $(cat "$packageConfigFile" | jq -r '.homepage')
Depends: docker, docker.io
EOT

	cat <<EOT >> "$packageDebDir/DEBIAN/copyright"
Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: $(cat "$packageConfigFile" | jq -r '.packageName')
Upstream-Contact: $(cat "$mainConfigFile" | jq -r '.copyright.contact')
Source: $(cat "$packageConfigFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$mainConfigFile" | jq -r '.copyright.copyright')
License: $(cat "$mainConfigFile" | jq -r '.copyright.licence')

EOT

	cat <<EOT >> "$packageDebDir/DEBIAN/preinst"
#!/bin/bash

#mkdir -p "/data/oqm/db/mongo"
#mkdir -p "/data/oqm/prometheus"
EOT
	chmod +x "$packageDebDir/DEBIAN/preinst"
	
	cat <<EOT >> "$packageDebDir/DEBIAN/postinst"
#!/bin/bash

systemctl daemon-reload
systemctl enable oqm_$curPackage.service
systemctl start oqm_$curPackage.service

#add config to file
mkdir -p /etc/oqm/serviceConfig
touch /etc/oqm/serviceConfig/infraConfig.list
EOT
	for row in $(jq -r '.configs[] | @base64' "$packageConfigFile"); do
		curConfig="$(echo ${row} | base64 --decode)"
		cat <<EOT >> "$packageDebDir/DEBIAN/postinst"
if grep -Fxq "$curConfig" /etc/oqm/serviceConfig/infraConfig.list
	then
		echo "Config value already present: $curConfig"
	else
		echo "$curConfig" >> /etc/oqm/serviceConfig/infraConfig.list
	fi
EOT
	done
	chmod +x "$packageDebDir/DEBIAN/postinst"
	
	cat <<EOT >> "$packageDebDir/DEBIAN/prerm"
#!/bin/bash

systemctl disable oqm_$curPackage.service
systemctl stop oqm_$curPackage.service

# remove config from infra config file
EOT
	for row in $(jq -r '.configs[] | @base64' "$packageConfigFile"); do
		curConfig="$(echo ${row} | base64 --decode)"
		cat <<EOT >> "$packageDebDir/DEBIAN/prerm"
sed -i -e "s!$curConfig!!g" /etc/oqm/serviceConfig/infraConfig.list
EOT
 	done
	chmod +x "$packageDebDir/DEBIAN/prerm"
	
	cat <<EOT >> "$packageDebDir/DEBIAN/postrm"
#!/bin/bash

systemctl daemon-reload
# Remove docker image
if [[ "$(docker images -q oqm_$curPackage 2> /dev/null)" != "" ]]; then
	docker rmi oqm_$curPackage
	echo "Removed docker image."
else
	echo "Docker image was already gone."
fi
if [ $( docker ps -a | grep oqm_$curPackage | wc -l ) -gt 0 ]; then
	docker rm oqm_$curPackage
	echo "Removed docker container."
else
	echo "Docker container was already gone."
fi


EOT
	chmod +x "$packageDebDir/DEBIAN/postrm"
	
	dpkg-deb --build "$packageDebDir" "$buildDir"
	
	#	
	# RPM build
	#
done
