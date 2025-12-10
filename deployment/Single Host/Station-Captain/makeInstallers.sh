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

debDir="StationCaptainDeb"
rpmDir="StationCaptainRpm"
outputDir="bin/"
userGuideFile="$buildDir/stationCaptainUserGuide.html"

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
cp docs/User\ Guide.adoc "$userGuideFile.temp"
sed -i '/link:README.md\[Back\]/d' "$userGuideFile.temp"
asciidoctor "$userGuideFile.temp" -o "$userGuideFile"
#
# Debian build
#

mkdir "$outputDir"
mkdir "$buildDir/$debDir"
mkdir "$buildDir/$debDir/DEBIAN"
mkdir "$buildDir/$debDir/bin"
mkdir -p "$buildDir/$debDir/usr/lib/oqm/station-captain"
mkdir -p "$buildDir/$debDir/usr/share/applications"
mkdir -p "$buildDir/$debDir/usr/share/doc/oqm"
mkdir -p "$buildDir/$debDir/etc/oqm/static"
mkdir -p "$buildDir/$debDir/etc/oqm/static/media"
mkdir -p "$buildDir/$debDir/etc/oqm/static/media/sc"
mkdir -p "$buildDir/$debDir/etc/oqm/snapshots/scripts/"
mkdir -p "$buildDir/$debDir/etc/oqm/accountScripts/"
mkdir -p "$buildDir/$debDir/etc/oqm/config/"
mkdir -p "$buildDir/$debDir/etc/oqm/certs/"
mkdir -p "$buildDir/$debDir/etc/bash_completion.d/"

install -m 755 -D src/oqm-captain.py "$buildDir/$debDir/bin/oqm-captain"
install -m 755 -D src/oqm-config.py "$buildDir/$debDir/bin/oqm-config"
install -m 755 -D src/oqm-station-captain-help.txt "$buildDir/$debDir/etc/oqm/static/"
install -m 755 -D src/integration/oqm-icon.svg "$buildDir/$debDir/etc/oqm/static/media/"
install -m 755 -D src/integration/oqm-sc-icon.svg "$buildDir/$debDir/etc/oqm/static/media/sc/"
install -m 755 -D src/integration/oqm-sc-guide-icon.svg "$buildDir/$debDir/etc/oqm/static/media/sc/"
install -m 755 -D src/integration/oqm-captain.desktop "$buildDir/$debDir/usr/share/applications/"
install -m 755 -D src/integration/oqm-captain-user-guide.desktop "$buildDir/$debDir/usr/share/applications/"
install -m 755 -D "$userGuideFile" "$buildDir/$debDir/usr/share/doc/oqm/stationCaptainUserGuide.html"
install -m 755 -D "src/snapshot-restore-base.sh" "$buildDir/$debDir/etc/oqm/snapshots/"
install -m 755 -D "src/account-assure-base.sh" "$buildDir/$debDir/etc/oqm/accountScripts/"
install -m 755 -D src/lib/* "$buildDir/$debDir/usr/lib/oqm/station-captain/"
install -m 755 -D src/mainConfig.json "$buildDir/$debDir/etc/oqm/config/"
install -m 755 -D src/certsReadme.md "$buildDir/$debDir/etc/oqm/certs/"
# Register files for cleanup when uninstalled
install -m 755 -D /dev/null "$buildDir/$debDir/etc/bash_completion.d/oqm-captain"
install -m 755 -D /dev/null "$buildDir/$debDir/etc/bash_completion.d/oqm-config"

sed -i "s/    SCRIPT_VERSION = 'SCRIPT_VERSION'/    SCRIPT_VERSION = '$(cat "$configFile" | jq -r '.version')'/" "$buildDir/$debDir/usr/lib/oqm/station-captain/ScriptInfos.py"
sed -i 's|sys.path.append("lib/")|sys.path.append("/usr/lib/oqm/station-captain/")|' "$buildDir/$debDir/bin/oqm-config"
sed -i 's|sys.path.append("lib/")|sys.path.append("/usr/lib/oqm/station-captain/")|' "$buildDir/$debDir/bin/oqm-captain"

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

# TODO:: updating bash completion doesn't work.
cat <<'EOT' > "$buildDir/$debDir/DEBIAN/postinst"
#!/bin/bash
#if [ -x "$(command -v register-python-argcomplete)" ]; then
#	echo "Using register-python-argcomplete"
#	register-python-argcomplete oqm-captain > /etc/bash_completion.d/oqm-captain
#	register-python-argcomplete oqm-config > /etc/bash_completion.d/oqm-config
#elif [ -x "$(command -v register-python-argcomplete3)" ]; then
#	echo "Using register-python-argcomplete3"
#	register-python-argcomplete3 oqm-captain > /etc/bash_completion.d/oqm-captain
#	register-python-argcomplete3 oqm-config > /etc/bash_completion.d/oqm-config
#else
#	echo "WARNING: could not run autocomplete!"
#fi

oqm-captain certs ensure-system-present

if [ ! -z "$(grep "oqm:" /etc/group)" ] ; then
	echo 'OQM group already existent'
else
	echo 'OQM group not yet existent. Creating'
	groupadd oqm
	echo 'OQM group created.'
fi

if id "oqm" >/dev/null 2>&1; then
	echo 'OQM user already existent'
else
	echo 'OQM user not yet existent. Creating.'
	adduser --system --shell /sbin/nologin --ingroup oqm oqm
	echo 'OQM user created.'
fi

EOT
chmod +x "$buildDir/$debDir/DEBIAN/postinst"

dpkg-deb --build --root-owner-group "$buildDir/$debDir" "$outputDir"
if [ $? -ne 0 ]; then
	exit 1;
fi

exit 0;
#TODO:: fix the following

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

sed -i "s/SCRIPT_VERSION = 'SCRIPT_VERSION'/SCRIPT_VERSION = '$(cat "$configFile" | jq -r '.version')'/" "$sourcesDir/oqm-config.py"
sed -i 's|sys.path.append("lib/")|sys.path.append("/usr/lib64/oqm/station-captain/")|' "$sourcesDir/oqm-config.py"

cp "$userGuideFile" "$sourcesDir/integration/stationCaptainUserGuide.html"

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

Requires:       $(cat "$configFile" | jq -r '.dependencies.rpm')

%description
A demo RPM build

%prep
%setup -q

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}%{_bindir}
mkdir -p %{buildroot}%{_libdir}/oqm/station-captain
mkdir -p %{buildroot}/etc/oqm/static
mkdir -p %{buildroot}/etc/oqm/snapshot/scripts
mkdir -p %{buildroot}/usr/share/applications

install -m 755 -D oqm-captain.py %{buildroot}/%{_bindir}/oqm-captain
install -m 755 -D oqm-config.py %{buildroot}/%{_bindir}/oqm-config
install -m 755 -D lib/* %{buildroot}%{_libdir}/oqm/station-captain/

install -m 755 -D snapshot-restore-base.sh %{buildroot}/etc/oqm/snapshot/
install -m 755 -D oqm-station-captain-help.txt %{buildroot}/etc/oqm/static/
install -m 755 -D integration/stationCaptainUserGuide.html %{buildroot}/etc/oqm/static/
install -m 755 -D integration/oqm-icon.svg %{buildroot}/etc/oqm/static/media
install -m 755 -D integration/oqm-sc-icon.svg %{buildroot}/etc/oqm/static/media
install -m 755 -D integration/oqm-sc-guide-icon.svg %{buildroot}/etc/oqm/static/media

install -m 755 -D integration/oqm-captain.desktop %{buildroot}/usr/share/applications/
install -m 755 -D integration/oqm-captain-user-guide.desktop %{buildroot}/usr/share/applications/

%clean
rm -rf ${buildroot}

%files
%{_bindir}/oqm-captain
%{_bindir}/oqm-config
%{_libdir}/oqm/station-captain/*
/etc/oqm/static/oqm-station-captain-help.txt
/etc/oqm/static/stationCaptainUserGuide.html
/etc/oqm/static/media/oqm-icon.svg
/etc/oqm/static/media/oqm-sc-icon.svg
/etc/oqm/static/media/oqm-sc-guide-icon.svg
/etc/oqm/snapshot/snapshot-restore-base.sh
/usr/share/applications/oqm-captain.desktop
/usr/share/applications/oqm-captain-user-guide.desktop

%changelog

EOT

echo "$buildDir/$rpmDir/SPECS/"

rpmlint "$buildDir/$rpmDir/SPECS/oqm-captain.spec"

rpmbuild --define "_topdir `pwd`/$buildDir/$rpmDir/" -bb "$buildDir/$rpmDir/SPECS/oqm-captain.spec"

cp -a "$buildDir/$rpmDir/RPMS/noarch/." "$outputDir"
if [ $? -ne 0 ]; then
	exit 1;
fi
