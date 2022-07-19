# Plugins

## Overview

## Auth/ Security

## Plugin Repositories

A Repository is a method for plugin distribution. It is described by a "`repo.json`" that tells the station captain what plugins exist for what versions of the [base station](../open-qm-base-station).

### File Schema:

```json
{
	"repo": {
		"maintainer": {
			"name": "repo maintainer name",
			"organization": "repo maintainer org",
			"email": "repo maintainer email",
			"phone": "repo maintainer phone"
		},
		"name": "name of the repo",
		"description": "description of the repo",
		"lastUpdate": "UTC timestamp when this repo file was last updated."
	},
	"ocpBaseVersion": {
		"1": [
			{
				"name": "plugin name",
				"description": "plugin description",
				"version": "plugin version",
				"packages": {
					"deb": {
						"link": "plugin link",
						"sha512": "sha512 of the .deb file"
					}
				}
			}
		]
	}
}
```

Notes:
- ALL versions are to be in
  the [Version format used by the core Open Quartermaster Software](Versioning_Branching_Tagging.md#versioning)
- Don't need to describe all versions of each plugin, just the most recent ones.
