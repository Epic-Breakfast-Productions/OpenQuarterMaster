#!/bin/bash

#
# Script to make installers for the Metrics.
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

debDir="metricsDeb"
rpmDir="metricsRpm"
outputDir="bin/"

sourcesDir="oqm-metrics-otel+lgtm-$(cat "$configFile" | jq -r '.version')"

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
mkdir "$buildDir/$debDir"
mkdir "$buildDir/$debDir/DEBIAN"
mkdir -p "$buildDir/$debDir/etc/systemd/system/"
mkdir -p "$buildDir/$debDir/etc/oqm/config/configs/"
mkdir -p "$buildDir/$debDir/etc/oqm/proxyConfig.d/"
mkdir -p "$buildDir/$debDir/etc/oqm/kcClients/"
mkdir -p "$buildDir/$debDir/usr/share/applications"
mkdir -p "$buildDir/$debDir/etc/oqm/serviceConfig/metrics/otel-lgtm/"


install -m 755 -D "uiEntry-grafana.json" "$buildDir/$debDir/etc/oqm/ui.d/oqm-metrics-otel_lgtm-grafana.json"
install -m 755 -D "30-metrics-otel_lgtm.json" "$buildDir/$debDir/etc/oqm/config/configs/"
install -m 755 -D "oqm-metrics-otel_lgtm.desktop" "$buildDir/$debDir/usr/share/applications/"
install -m 755 -D "oqm-metrics-otel_lgtm-proxy-config.json" "$buildDir/$debDir/etc/oqm/proxyConfig.d/"
install -m 755 -D "oqm-metrics-otel_lgtm-grafana-client.json" "$buildDir/$debDir/etc/oqm/kcClients/"
install -m 755 -D "metrics-otel_lgtm-config.list" "$buildDir/$debDir/etc/oqm/serviceConfig/metrics/otel-lgtm/main-config.list"

serviceFile="oqm-metrics-otel_lgtm.service"
serviceFileEscaped="$serviceFile" # "$(systemd-escape "$serviceFile")"

install -m 755 "$serviceFile" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"
sed -i "s/\${version}/$(cat "$configFile" | jq -r '.version')/" "$buildDir/$debDir/etc/systemd/system/$serviceFileEscaped"

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
Upstream-Name: Otel LGTM
Upstream-Contact: $(cat "$configFile" | jq -r '.copyright.contact')
Source: $(cat "$configFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$configFile" | jq -r '.copyright.copyright')
License: $(cat "$configFile" | jq -r '.copyright.licence')
EOT


cat <<EOT >> "$buildDir/$debDir/DEBIAN/preinst"
#!/bin/bash

mkdir -p /etc/oqm/serviceConfig/metrics/otel-lgtm/

if [ ! -f "/etc/oqm/serviceConfig/metrics/otel-lgtm/user-config.list" ]; then
	cat <<EOF >> "/etc/oqm/serviceConfig/metrics/otel-lgtm/user-config.list"
# Add your own config here.

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
if [[ "$(docker images -q oqm-metrics-otel_lgtm 2> /dev/null)" != "" ]]; then
        docker rmi oqm-metrics-otel_lgtm
        echo "Removed docker image."
else
        echo "Docker image was already gone."
fi
if [ $( docker ps -a | grep oqm-metrics-otel_lgtm | wc -l ) -gt 0 ]; then
        docker rm oqm-metrics-otel_lgtm
        echo "Removed docker container."
else
        echo "Docker container was already gone."
fi

EOT
chmod +x "$buildDir/$debDir/DEBIAN/postrm"

dpkg-deb --build --root-owner-group "$buildDir/$debDir" "$buildDir"