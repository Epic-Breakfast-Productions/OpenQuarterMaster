# oqm-core-api

![Docker Image Version](https://img.shields.io/docker/v/ebprod/oqm-core-api?label=Docker%20Image)


This project is the server providing the core functionality of Open QuarterMaster.

Additional docs can be found in the [docs](docs/README.md) folder, along with at the main project [readme](../../README.md). 

## TL;DR getting started:

More detail in [this guide](docs/development/DevelopmentGettingStarted.md)

Prerequisites:

 1. Java runtime and dev kit version 21 (official or open is fine)
 2. Docker (podman might also work, with some tweaks)

### Common Commands

#### Running in dev mode

Dev mode allows you to run this service in a way that facilitates development. Changes are automatically recompiled and served, along with dev resources like Mongo and Kafka being stood up for you.

```shell script
./gradlew quarkusDev
```

Dev ui at: http://localhost:8080/q/dev/

Swagger at: http://localhost:8080/openapi-ui/index.html

#### Tests

Regular set of tests:

```shell script
./gradlew test
```

