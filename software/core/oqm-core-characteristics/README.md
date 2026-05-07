# OQM Core Characteristics Service

[Back](../README.md)

This service is responsible for serving:

 - "characteristic" data; information about the running system to label or otherwise identify it:
   - "Run By" information
   - Banner declaration; I.E, for system classification
 - Available UI data; what UI's are available and how to get to them for easy navigation and integration

Further documentation available [here](./docs/README.md).

## Developing

### Virtual environment:

#### Initial Setup:

 1. `python -m venv .venv`
 2. `source .venv/bin/activate`
 3. `pip install -r requirements.txt`

#### Activate venv:

`source .venv/bin/activate`

### Running

`fastapi dev app/main.py --port 8080`

### Building container

`docker buildx build --platform linux/amd64,linux/arm64 -t ebprod/oqm-core-characteristics:$(cat "./installerSrc/installerProperties.json" | jq -r '.version') .`

For Release:

`docker push ebprod/oqm-core-characteristics:$(cat "./installerSrc/installerProperties.json" | jq -r '.version')`

### Tests

#### Setup

In your virtual env, run: `pip install -r requirements-test.txt`

#### Running

Simply run `pytest` in the project root.

## Helpful Tips n Tricks

`docker run --rm --network oqm-internal curlimages/curl:latest -L -v http://oqm-core-characteristics/all | jq`

`time docker run --rm --network oqm-internal curlimages/curl:latest -L -vvvv -w 'Total: %{time_total}s\n' http://oqm-core-characteristics/all`

## TODOs:

 - Characteristics:
   - Handle images from URL?
