# OQM Characteristics Service Configuration

This document goes over how to configure the Characteristics server.

## Characteristics

The characteristics are loaded in via a file specified by the `CHARACTERISTICS_FILE` environment var, or `/data/characteristics.yaml` by default.

The values can be overridden by associated environment variables, also indicated in the below example.

Schema of this file, all fields optional, except where noted:

```yaml

# What to call this instance
#    CHARACTERISTICS_VAL_TITLE
title:
   
# A message to display
#    CHARACTERISTICS_VAL_MOTD
motd:

# Information directly about who's running the system
runBy:
  # The name of the group running the system
  #    CHARACTERISTICS_VAL_RUNBY_NAME
  name:
  # The email of the group running the system
  #    CHARACTERISTICS_VAL_RUNBY_EMAIL
  email:
  # The phone number of the group running the system
  #    CHARACTERISTICS_VAL_RUNBY_PHONE
  phone:
  # The website of the group running the system
  #    CHARACTERISTICS_VAL_RUNBY_WEBSITE
  website:
  # The path to where to load the logo image *
  #    CHARACTERISTICS_VAL_RUNBY_LOGOIMG
  logoImg:
  # The path to where to load the banner image *
  #    CHARACTERISTICS_VAL_RUNBY_BANNERIMG
  bannerImg:
  
# Defines a banner to be displayed at the top of the screen. Example would be a classification marking. All fields are mandatory if specifying a banner.
banner:
  # The text to display.
  #    CHARACTERISTICS_VAL_BANNER_TEXT
  text:
  # The color of the text +
  #    CHARACTERISTICS_VAL_BANNER_TEXTCOLOR
  textColor:
  # The color of the background +
  #    CHARACTERISTICS_VAL_BANNER_BACKGROUNDCOLOR
  backgroundColor:
```

`*` = Paths can be given either in full (`/path/to/file.jpg`), or in a directory specified by `CHARACTERISTICS_RUNBY_IMG_DIR`.

`+` = Colors are specified by either names or hex (`#000000`) values. Any value considered valid by the HTML color spec is to be considered valid.


## UI's

UIs are defined by files listed in the direcgtory specified by `UIS_DATA_DIR`, or `/data/uis/` by default.

Format of each file:

```json
{
   "type": "Core",
   "order": 0,
   "id": "oqm-core-base_station",
   "name": "Base Station",
   "description": "The Main UI for Open QuarterMaster. If you are unsure where to start, start here!",
   "url": "http://foo",
   "urlConfigKey": "core.baseStation.externalBaseUri",
   "icon": "/core/api/core-api.svg",
   "monitorEndpoint": "/q/health",
   "endpoints": {
      "item": {
         "view": "/items?item={item}"
      }
   }
}
```

These get interpolated into the format that is returned by the app.

Image paths are relative to the directory specified by the `CHARACTERISTICS_UIS_ICON_DIR` env var.

## Single Node Host

The Characteristics server is configured in the Single Node Host deployment using standard means.

Relevant config is in `core.characteristics` (`oqm-config get core.characteristics`).

The service is setup to find RunBy images in `/etc/oqm/serviceConfig/core/characteristics/runBy/`, so feel free to place images in that directory, referencing them by just file name in the configuration. 
