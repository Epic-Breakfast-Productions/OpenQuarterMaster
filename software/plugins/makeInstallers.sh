#!/bin/bash

#
# Script to make installers for The plugins of Open QuarterMaster.
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

debDir="PluginsDeb"

# All
packages=("mss-controller")
# Ready for deployment
#packages=("mss-controller-plugin")

#
# Clean
#

rm -rf "$buildDir"

#
# Setup
#

mkdir "$buildDir"

for curPackage in ${packages[@]}; do
	packageInstallerSrc="$curPackage/snhInstallerSrc"
	packageConfigFile="$packageInstallerSrc/properties.json"
	packageDebDir="$buildDir/$curPackage/$debDir"
	pushd ./$curPackage/
	pluginVersion="$(./gradlew -q printVersion)"
	popd
	echo "Creating deb installer for $curPackage version $pluginVersion"

	#
	# Debian build
	#
	mkdir -p "$packageDebDir"
	mkdir "$packageDebDir/DEBIAN"
	mkdir -p "$packageDebDir/etc/systemd/system/"
	mkdir -p "$packageDebDir/etc/oqm/config/configs/"

	serviceFiles=()

	for i in `find "./$packageInstallerSrc" -name "*.service" -type f -printf '%f\n'`; do
		[ -f "./$packageInstallerSrc/$i" ] || break

		serviceFile="$i"
		echo "Service file: $serviceFile"
#		serviceFileEscaped="$(systemd-escape "$serviceFile")" # TODO:: verify works
		serviceFileEscaped="$serviceFile"
		cp "$packageInstallerSrc/$serviceFile" "$packageDebDir/etc/systemd/system/$serviceFileEscaped"
		sed -i "s/\${version}/$pluginVersion/" "$packageDebDir/etc/systemd/system/$serviceFileEscaped"
		serviceFiles+=("$serviceFileEscaped")
	done

	cp "$packageInstallerSrc/50-plugin-$curPackage.json" "$packageDebDir/etc/oqm/config/configs/"

	if [ -f "$packageInstallerSrc/$curPackage-snapshot-restore.sh" ]; then
		mkdir -p "$packageDebDir/etc/oqm/snapshots/scripts/"
		cp "$packageInstallerSrc/$curPackage-snapshot-restore.sh" "$packageDebDir/etc/oqm/snapshots/scripts/"
	fi
	if [ -f "$packageInstallerSrc/infra-$curPackage-proxy-config.json" ]; then
		mkdir -p "$packageDebDir/etc/oqm/proxyConfig.d/"
		cp "$packageInstallerSrc/infra-$curPackage-proxy-config.json" "$packageDebDir/etc/oqm/proxyConfig.d/"
	fi
	if [ -f "$packageInstallerSrc/plugin-$curPackage-client.json" ]; then
		mkdir -p "$packageDebDir/etc/oqm/kcClients/"
		cp "$packageInstallerSrc/plugin-$curPackage-client.json" "$packageDebDir/etc/oqm/kcClients/"
	fi

	# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
	# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
	cat <<EOT >> "$packageDebDir/DEBIAN/control"
Package: $(cat "$packageConfigFile" | jq -r '.packageName')
Version: $pluginVersion
Section: Open QuarterMaster
Maintainer: $(cat "$mainConfigFile" | jq -r '.maintainer.name')
Developer: EBP
Architecture: all
Description: $(cat "$packageConfigFile" | jq -r '.description')
Homepage: $(cat "$packageConfigFile" | jq -r '.homepage')
Depends: docker, docker.io, open+quarter+master-manager-station+captain (>= 2.0.0)$(cat "$packageConfigFile" | jq -r '.dependencies.deb')
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

EOT
	chmod +x "$packageDebDir/DEBIAN/preinst"

	cat <<EOT >> "$packageDebDir/DEBIAN/postinst"
#!/bin/bash

EOT
	chmod +x "$packageDebDir/DEBIAN/postinst"


	cat <<EOT >> "$packageDebDir/DEBIAN/prerm"
#!/bin/bash

EOT
	chmod +x "$packageDebDir/DEBIAN/prerm"

	cat <<EOT >> "$packageDebDir/DEBIAN/postrm"
#!/bin/bash

EOT
	chmod +x "$packageDebDir/DEBIAN/postrm"

	if [ ${#serviceFiles[@]} -ne 0 ]; then
		cat <<EOT >> "$packageDebDir/DEBIAN/postinst"
systemctl daemon-reload

systemctl enable ${serviceFiles[@]@Q}
systemctl start ${serviceFiles[@]@Q}

EOT
		cat <<EOT >> "$packageDebDir/DEBIAN/prerm"
systemctl disable ${serviceFiles[@]@Q}
systemctl stop ${serviceFiles[@]@Q}

echo "Stopped ${serviceFiles[@]@Q}"
EOT
		cat <<EOT >> "$packageDebDir/DEBIAN/postrm"
systemctl daemon-reload
EOT
	fi

	fileKeys=($(jq -r '.files | keys[]'  "$packageConfigFile"))

	for fileKey in ${fileKeys[@]}; do
		curFile="$packageDebDir$fileKey"

		mkdir -p "$(dirname "$curFile")"
		fileInSrc="$(jq -r ".files.\"$fileKey\""  "$packageConfigFile")"
#		echo "Config file content: $curConfigFileContent"
		echo "Adding file: $fileInSrc to directory $fileKey";
		cp -r "$packageInstallerSrc/$fileInSrc" "$curFile"
	done;

	dpkg-deb --build "$packageDebDir" "$buildDir"

	#
	# RPM build
	#
done
