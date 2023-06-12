
function cron_enable(){
	local file="oqm-$1"
	local frequency="$2"
	local script="$3"

	case "$frequency" in
		"hourly"|"daily"|"weekly"|"monthly")
		;;
		*)
			echo "Invalid frequency value. Must be 'hourly', 'daily', 'weekly', or 'monthly'. Value given: $frequency"
			return 1
		;;
	esac

	local cronfile="/etc/cron.$frequency/$file"

	cat <<EOT >> "$cronfile"
#!/bin/bash
# This script placed here by oqm-captain.
$script
EOT

}

function cron_disable(){
	local file="oqm-$1"

	rm "/etc/cron.hourly/$file"
	rm "/etc/cron.daily/$file"
	rm "/etc/cron.weekly/$file"
	rm "/etc/cron.monthly/$file"
}

function cron_enablePeriodicSnapshots(){
	local frequency="$(oqm-config -g snapshots.frequency)"
	local script="oqm-captain -s auto"
	local file="snapshots"

	cron_enable "$file" "$frequency" "$script"
}

function cron_disablePeriodicSnapshots(){
	cron_disable "snapshots"
}
