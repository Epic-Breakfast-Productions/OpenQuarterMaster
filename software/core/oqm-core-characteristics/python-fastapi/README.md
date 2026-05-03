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
 - Service Listing
 

## Interface

### Characteristics

The endpoint to get characteristics data is:

`/characteristics`

Format:

```json

```

These values match 1:1 in the data outlined below.

## Configuration

### Characteristics

The characteristics are loaded in via a file specified by `characteristics.fileLocation`.

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

This feature has yet to be implemented. Stay tuned!

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


