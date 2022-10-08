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


## to look into

- https://medium.com/picovoice/prioritizing-privacy-add-offline-speech-recognition-to-a-java-application-1c864574fb7e

## TODOS:

- Expiry
  - tests
- Other
  - add "server admin" role
- db
  - look into liquibase: https://quarkus.io/guides/liquibase-mongodb
- Overview
  - create "explore" and "stats" section; start at explore.
  - explore
    - add images
  - stats
    - \# of items, blocks
    - come up with helpful stats to show
- Storage
  - do all stats
  - handle breadcrumb links
  - bulk create
  - bulk edit?
- Items
  - stats
  - UI tweaks
    - view improvements
  - External item lookup.
    - support throwing data points to adding/editing item  
    - API's:
      - Good leads:
        - https://opengtindb.org/index.php
        - https://upcdatabase.org/api
      - Backups:
        - https://devapi.barcodespider.com/
- Overview
  - stats
- Image management
  - overall stats
  - responsive design tweaks
  - testing for large images
  - Allow for image upload on image select
  - Look at moving to Cropper to replace Croppie: https://github.com/fengyuanchen/cropperjs
- User
  - external
    - view your infos
  - self
    - review acct create
    - view your info
    - give admins
      - add user
      - remove user
      - promote/demote user
      - search users
- REST
  - handle ParseException for expired, bad tokens for UI
- Testing