# Development getting started

[Back](README.md)

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using:
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/open-qm-base-station-0.0.1-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Running dev mode tooling

`docker run --name oqm_mongo -p=27017:27017 -d mongo`

`docker run --name jaeger -p 8090:16686 -p 8091:14268 jaegertracing/all-in-one:latest`

`docker start oqm_mongo jaeger`

## Creating clients from openapi spec

 1. Run server in devmode
 2. Download openApi spec to `generatedClients/`: http://localhost:8080/q/openapi
 3. Ensure file downloaded has `.yaml` extension
 4. Run the following command in this project's root:
    - Java: 
        ```shell
        docker run --rm -v $PWD/generatedClients:/local openapitools/openapi-generator-cli generate -i /local/openapi.yaml \
            -g java \
            -o /local/out/java
        ```

### Sources

 - https://openapi-generator.tech/docs/generators
