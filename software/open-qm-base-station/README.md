# open-qm-base-station

This project is the server providing the core functionality of Open QuarterMaster.

Additional docs can be found in the [docs/](docs/README.md) folder, along with at the main project [readme](../../README.md). 

## TL;DR getting started:

More detail in [this guide](docs/Development%20Getting%20Started.md)

Prerequisites:

 1. Running instance of MongoDB (preferably at `mongodb://localhost:27017`, to avoid needing to set config)
 2. Java runtime and dev kit version 11 (official or open is fine)
 3. Running `./gradlew publishToMavenLocal` on the core lib project: [open-qm-core](../libs/open-qm-core)

### Common Commands

#### Running in dev mode

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

Native-specific tests:

```shell script
./gradlew testNative
```











# to look into

- https://medium.com/picovoice/prioritizing-privacy-add-offline-speech-recognition-to-a-java-application-1c864574fb7e

# TODOS:

- Storage
  - Finish add
  - do edit
  - do view
  - do all stats
- Items
  - Add item
  - edit item
  - delete item
  - stats
- Overview
  - stats
- User
  - external
    - view your infos
  - self
    - view your info
    - give admins
      - add user
      - remove user
      - promote/demote user
      - search users
- REST
  - handle ParseException for expired, bad tokens for UI
- Image/thumbnail support
- Testing