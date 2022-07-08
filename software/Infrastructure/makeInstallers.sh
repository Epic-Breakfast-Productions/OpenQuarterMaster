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

packages=("jaeger" "mongo")

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
	sed -i "s/\${version}/$(cat "$packageConfigFile" | jq -r '.version')/" "$packageDebDir/etc/systemd/system/oqm_$curPackage.service"
	
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
Depends: docker
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

	cat <<EOT >> "$packageDebDir/DEBIAN/postinst"
#!/bin/bash

systemctl enable oqm_$curPackage.service
systemctl start oqm_$curPackage.service
EOT
	chmod +x "$packageDebDir/DEBIAN/postinst"
	
	cat <<EOT >> "$packageDebDir/DEBIAN/prerm"
#!/bin/bash

systemctl disable oqm_$curPackage.service
systemctl stop oqm_$curPackage.service
EOT
	chmod +x "$packageDebDir/DEBIAN/prerm"
	
	cat <<EOT >> "$packageDebDir/DEBIAN/postrm"
#!/bin/bash

# Remove docker image?
EOT
	chmod +x "$packageDebDir/DEBIAN/postrm"
	
	dpkg-deb --build "$packageDebDir" "$buildDir"
	
	#	
	# RPM build
	#
done
