# open-qm-base-station

This project is the server providing the core functionality of Open QuarterMaster.

Additional docs can be found in the [docs/](docs/README.md) folder, along with at the main project [readme](../../README.md). 

## TL;DR getting started:

More detail in [this guide](docs/Development%20Getting%20Started.md)

Prerequisites:

 1. Running instance of MongoDB (preferably at `mongodb://localhost:27017`, to avoid needing to set config)
 2. Java runtime and dev kit version 11 (official or open is fine)

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

### General:

- Expiry
  - tests
- Other
- db
  - look into liquibase: https://quarkus.io/guides/liquibase-mongodb
- Overview
  - explore
    - add images
  - stats
    - \# of items, blocks
    - come up with helpful stats to show
- Objects In General
  - History view to include search, paging
- Storage
  - do all stats
- Items
  - fix search form
  - fix unit editing unit
  - stats
  - link to storage block page on item view
  - better block name in view
  - low stock threshold/notification
  - UI tweaks
    - view improvements
  - External item lookup.
    - support throwing data points to adding/editing item
    - API's:
      - Good leads:
        - https://opengtindb.org/index.php
        - https://upcdatabase.org/api
        - https://www.mcmaster.com/help/api/
      - Backups:
        - https://devapi.barcodespider.com/
- Overview
  - stats
- Image management
  - overall stats
  - responsive design tweaks
  - testing for large images
  - Look at moving to Cropper to replace Croppie: https://github.com/fengyuanchen/cropperjs
- User
  - external
    - view your infos
  - self
    - stats? 
    - figure out how to handle arbitrary roles from ext services/ plugins
  - give admins
    - search users
- REST
  - handle ParseException for expired, bad tokens for UI
- Testing
- Other
  - file attachments

### From User feedback

- import
  - from csv
    - determine what this should look like
    - provide template
  - from excel?
- select:
  - better labeling of search items
- items
  - add/edit
    - open new stored dropdown when added
  - add/sub/trans
    - better placement of submit button