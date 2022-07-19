#!/bin/bash
# Script to generate the repo file for this git repo

REPO_FILE="plugin-repo.json"
PLUGIN_DIRS=("open-qm-plugin-demo")

read -r -d '' jsonOutput << EOM
{
	"repo": {
		"maintainer": {
			"name": "Greg Stewart",
			"organization": "Epic Breakfast Productions",
			"email": "contact@gjstewart.net"
		},
		"name": "Open QuarterMaster Official",
		"description": "The official plugin repository for Open QuarterMaster.",
		"lastUpdate": "$(date -u)"
	},
	"ocpBaseVersion": {
	}
}
EOM

# TODO:: read through plugin dirs, populate "ocpBaseVersion"

echo "$jsonOutput" > "$REPO_FILE"