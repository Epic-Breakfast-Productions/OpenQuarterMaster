# Quarkus Basics

This project uses [Quarkus](https://quarkus.io/) for all Java based services.
The commands below summarize the typical workflow when working with any of the
modules in this repository.

## Running in dev mode

Use dev mode to get fast feedback while coding:

```bash
./gradlew quarkusDev
```

A Dev UI is available at `http://localhost:8080/q/dev/` while running in this
mode.

## Packaging the application

Build a JVM package with:

```bash
./gradlew build
```

The resulting `quarkus-run.jar` and supporting libraries are placed under
`build/quarkus-app/`. Run it with:

```bash
java -jar build/quarkus-app/quarkus-run.jar
```

To create an _über-jar_ instead, append `-Dquarkus.package.type=uber-jar` to the
build command and run the jar placed in `build/*-runner.jar`.

## Building a native executable

Native executables can be produced using GraalVM:

```bash
./gradlew build -Dquarkus.package.type=native
```

If GraalVM is not available locally use container based build:

```bash
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

The generated binary is located in the `build/` directory and ends with
`-runner`.
