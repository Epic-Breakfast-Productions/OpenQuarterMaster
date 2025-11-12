# storagotchi

This app is meant to be a cute semi-interactable entity to play with in the background. Support your Storagotchi by performing storage related tasks!

## Features

 - Storagotchi
   - happiness / energy level based on recent activity
   - animated
 - Environment
   - Buildable, points from storage actions
 - stream changes to state via websocket
 - Notifications
   - If enabled, notify thru browser when things happen and state changes

Game Mechanics

 - Initial state:
   - boxes around the page
   - Some items smattered around
   - Storagotchi in center
   - calculated number of items lying around for the number of OQM inventory actions taken recently
 - Storagotchi:
   - goes to boxes, picks things up, carries them around
   - drops said items
   - gets overwhelmed when too cluttered (too many items on screen)
   - gets inspired by your inventory actions, cleans up one item per action
   - does something fun when clicked
     - giggle
     - dance
     - (rare) quotes
       - https://www.goodreads.com/work/quotes/41711738-jinsei-ga-tokimeku-katazuke-no-maho
 - Boxes:
   - jump / move slightly when clicked
   - "give" items to Storagotchi
   - "take" items from Storagotchi
 - stuff
   - wiggles when clicked

## Credits

Resources from:

 - https://last-tick.itch.io/16x16-icons-tools-electronic-gadgets
 - https://apokalips123.itch.io/pixel-warehouse-stuff


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/storagotchi-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, Jakarta Persistence)
- Qute ([guide](https://quarkus.io/guides/qute)): Offer templating support for web, email, etc in a build time, type-safe way
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code for Hibernate ORM via the active record or the repository pattern

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)


### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
