# OQM Core Characteristics Service

This service is responsible for serving:

 - "characteristic" data; information about the running system to label or otherwise identify it:
   - "Run By" information
   - Logo / Banner
   - System Banner
 - Available UI data; what UI's are available and how to get to them for easy navigation and integration

## TODOs:

 - Characteristics:
   - handle SVG images
   - Handle images from URL?
 - Caching of data read from file
 

## Interface

### All

`/all` will return all of the following datas, like:

```json
{
   "characteristics" : {
      // data from /characteristics
   },
   "uis": {
      // data from /uis
   }
}
```

### Characteristics

The endpoint to get characteristics data is:

`/characteristics`

Format:

```json

```


These values match 1:1 in the characteristics file data outlined below.


#### Images

To actually retrieve image data (if available), use

`/characteristics/logo`

and

`/characteristics/banner`

(if no image was available, these endpoints will return with a `400` error)


### UIs

These values are a list of ui's available on the system. This data is used to infer where else the user can go.

Endpoint: `/uis`

Format:

```json
{
   "core": [
      // UI entries
   ],
   "plugin": [
      // UI entries
   ],
   "metrics": [
      // UI entries
   ],
   "infra": [
      // UI entries
   ]
}
```

Where a single UI entry is described as:

```json
{
   "name": "",
   "description": "",
   "baseUri": "",
   "icon": true,
   "endpoints": {
      "health": "/q/health",
      "item": {
         "view": "/items?item={item}"
      }
   }
}
```

#### Icons

To actually retrieve image data (if available), use

`/uis/{category}/{index}/icon`

Where `category` is the type of ui, like `plugin`, and `index` is the index of the entry in the array of results.

(if no image was available, these endpoints will return with a `400` error)

## Configuration

### Characteristics

The characteristics are loaded in via a file specified by the `CHARACTERISTICS_FILE` environment var, or `/data/characteristics.yaml` by default.

Schema of this file (shown in `yaml` for comments, all fields optional, except where noted):

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
  name:
  # The email of the group running the system
  email:
  # The phone number of the group running the system
  phone:
  # The website of the group running the system
  website:
  # The path to where to load the logo image *
  logoImg:
   # The path to where to load the banner image *
  bannerImg:
  

# Defines a banner to be displayed at the top of the screen. Example would be a classification marking. All fields are mandatory if specifying a banner.
banner:
  # The text to display.
  text:
  # The color of the text +
  textColor:
  # The color of the background +
  backgroundColor:
```

`*` = Paths can be given either in full (`/path/to/file.jpg`) or relative to where the configuration file is located (`file.jpg`, will be looked for in `/path/to/file.jpg` if the config file is `/path/to/conf.yaml`)

`+` = Colors are specified by either names or hex (`#000000`) values.

### UI's

UIs are defined by files listed in the `/data/uis/` directory.

Format of each file:

```json
{
   "type": "Core",
   "order": 0,
   "name": "Base Station",
   "description": "The Main UI for Open QuarterMaster. If you are unsure where to start, start here!",
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

## Developing

### Virtual environment:

#### Initial Setup:

 1. `python -m venv .venv`
 2. `pip install -r requirements.txt`

#### Activate venv:

`source .venv/bin/activate`

### Running

`fastapi dev app/main.py --port 8080`

### Building container

`docker build -t ebprod/oqm-core-characteristics .`

### Tests


