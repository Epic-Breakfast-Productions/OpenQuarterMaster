# oqm-core-characteristics

TODOS:

 - [ ] Error mapper for reading in config errors
 - [ ] Characteristic imagry validaition
 - [ ] Characteristic imagry serving
 - [ ] UI Reading / parsing
 - [ ] UI serving
 - [ ] Tests
    - [ ] Characteristics read in JSON
    - [ ] Characteristics read in YAML
    - [ ] Characteristics re-loaded on change
    - [ ] UI read in Base Station format
    - [ ] UI read in json format
    - [ ] UI re-loaded on change


This service is responsible for serving:

 - "characteristic" data; information about the running system to label or otherwise identify it:
   - "Run By" information
   - Logo / Banner
   - System Banner
 - Available UI data; what UI's are available and how to get to them for easy navigation and integration

## Configuration

### Characteristics

The characteristics are loaded in via a file specified by `characteristics.fileLocation`.

Schema of this file (shown in `yaml` for comments, all fields optional, except where noted):

```yaml

# What to call this instance
title:
   
# A message to display
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

\* = Paths can be given either in full (`/path/to/file.jpg`) or relative to where the configuration file is located (`file.jpg`, will be looked for in `/path/to/file.jpg` if the config file is `/path/to/conf.yaml`)

\+ = Colors are specified by either names or hex (`#000000`) values.




# Quarkus Stuff

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

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

You can then execute your native executable with: `./build/oqm-core-characteristics-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Document your REST APIs with OpenAPI - comes with Swagger UI
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- YAML Configuration ([guide](https://quarkus.io/guides/config-yaml)): Use YAML to configure your Quarkus application
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health
- Cache ([guide](https://quarkus.io/guides/cache)): Enable application data caching in CDI beans

## Provided Code

### YAML Config

Configure your application with YAML

[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

The Quarkus application configuration is located in `src/main/resources/application.yml`.

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### SmallRye Health

Monitor your application's health using SmallRye Health

[Related guide section...](https://quarkus.io/guides/smallrye-health)
