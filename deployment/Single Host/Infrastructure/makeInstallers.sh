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

cd "$(dirname "$0")" || exit

mainConfigFile="properties.json"
buildDir="build"

debDir="InfraDeb"

# Testing
#packages=("jaeger" "mongo" "postgres" "keycloak" "nginx")
#packages=("mongo" "postgres" "nginx")
packages=("mongo" "postgres" "keycloak" "traefik")
# Ready for deployment
packages=("traefik" "mongo" "postgres" "keycloak" "kafka-red-panda")
#packages=("jaeger" "mongo" "postgres" "keycloak")

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
	mkdir -p "$packageDebDir/etc/oqm/config/configs/"
	mkdir -p "$packageDebDir/etc/oqm/snapshots/scripts/"
	mkdir -p "$packageDebDir/etc/oqm/accountScripts/"

	serviceFiles=()

	for i in `find "./$curPackage" -name "*.service" -type f -printf '%f\n'`; do
		[ -f "./$curPackage/$i" ] || break

		serviceFile="$i"
		echo "Service file: $serviceFile"
		serviceFileEscaped="$serviceFile" #"$(systemd-escape "$serviceFile")"
		cp "$curPackage/$serviceFile" "$packageDebDir/etc/systemd/system/$serviceFileEscaped"
		sed -i "s/\${version}/$(jq -r '.version' "$packageConfigFile")/" "$packageDebDir/etc/systemd/system/$serviceFileEscaped"
		serviceFiles+=("$serviceFileEscaped")
	done

	cp "$curPackage/10-$curPackage.json" "$packageDebDir/etc/oqm/config/configs/"

	if [ -f "$curPackage/$curPackage-snapshot-restore.sh" ]; then
		cp "$curPackage/$curPackage-snapshot-restore.sh" "$packageDebDir/etc/oqm/snapshots/scripts/"
	fi
	if [ -f "$curPackage/$curPackage-assert-account.sh" ]; then
		cp "$curPackage/$curPackage-assert-account.sh" "$packageDebDir/etc/oqm/accountScripts/"
	fi
	if [ -f "$curPackage/infra-$curPackage-proxy-config.json" ]; then
		mkdir -p "$packageDebDir/etc/oqm/proxyConfig.d/"
		cp "$curPackage/infra-$curPackage-proxy-config.json" "$packageDebDir/etc/oqm/proxyConfig.d/"
	fi

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
Depends: docker.io, oqm-manager-station+captain (>= 2.2.0)$(cat "$packageConfigFile" | jq -r '.dependencies.deb')
EOT
	# TODO:: add conflicts

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
# restart proxy after we add config
#if [ $(systemctl list-unit-files "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service" | wc -l) -gt 3 ]; then
#	systemctl restart "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service"
#fi
systemctl enable ${serviceFiles[@]@Q}
systemctl start ${serviceFiles[@]@Q}

EOT

	chmod +x "$packageDebDir/DEBIAN/postinst"
	
	cat <<EOT >> "$packageDebDir/DEBIAN/prerm"
#!/bin/bash

systemctl disable ${serviceFiles[@]@Q}
systemctl stop ${serviceFiles[@]@Q}

echo "Stopped ${serviceFiles[@]@Q}"

EOT

	chmod +x "$packageDebDir/DEBIAN/prerm"
	
	cat <<EOT >> "$packageDebDir/DEBIAN/postrm"
#!/bin/bash

systemctl daemon-reload

# Remove docker image
docker stop oqm_$curPackage || echo "Docker container stopped previously."

if [ $( docker ps -a | grep oqm_$curPackage | wc -l ) -gt 0 ]; then
	docker rm oqm_$curPackage
	echo "Removed docker container."
else
	echo "Docker container was already gone."
fi
if [[ "$(docker images -q oqm_$curPackage 2> /dev/null)" != "" ]]; then
	docker rmi oqm_$curPackage
	echo "Removed docker image."
else
	echo "Docker image was already gone."
fi

#if [ $(systemctl list-unit-files "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service" | wc -l) -gt 3 ]; then
#	systemctl restart "open\\x2bquarter\\x2bmaster\\x2dinfra\\x2dnginx.service"
#fi
EOT

	chmod +x "$packageDebDir/DEBIAN/postrm"

	configFileKeys=($(jq -r '.configFiles | keys[]'  "$packageConfigFile"))

	for configFileKey in ${configFileKeys[@]}; do
		echo "Adding config file: $configFileKey";
		curConfigFile="$packageDebDir$configFileKey"

		mkdir -p "$(dirname "$curConfigFile")"

		curConfigFileInSrc="$(jq -r ".configFiles.\"$configFileKey\""  "$packageConfigFile")"
		echo "Adding config file: $curConfigFileInSrc to directory $configFileKey";
		cp "$curPackage/$curConfigFileInSrc" "$curConfigFile"
	done;


	fileKeys=($(jq -r '.files | keys[]'  "$packageConfigFile"))

	for fileKey in ${fileKeys[@]}; do
		curFile="$packageDebDir$fileKey"

		mkdir -p "$(dirname "$curFile")"
		fileInSrc="$(jq -r ".files.\"$fileKey\""  "$packageConfigFile")"
#		echo "Config file content: $curConfigFileContent"
		echo "Adding file: $fileInSrc to directory $fileKey";
		cp -r "$curPackage/$fileInSrc" "$curFile"
	done;

	dpkg-deb --build "$packageDebDir" "$buildDir"
	
	#	
	# RPM build
	#
done
