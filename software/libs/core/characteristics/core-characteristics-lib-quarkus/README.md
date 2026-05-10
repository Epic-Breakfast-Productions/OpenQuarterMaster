# OQM Core API Library for Quarkus

This library is made for utilization in a Quarkus app. It provides a devservice to connect to, and enables communication to the core api from another service.

## Utilities

### API

### HealthCheck

### Database Service

## Usage:

1. Include in project
2. Configuration:
   ```yaml

   oqm:
     core:
       characteristics:
         devservices:
           enabled: true
   ```

## Deployment

https://central.sonatype.org/publish/publish-portal-maven/

`./mvnw deploy`

then:

https://central.sonatype.com/publishing/deployments

## Tips n Tricks

 - Connect to kafka with UI: `docker run --network=host -e KAFKA_BROKERS=localhost:9192 -e SERVER_LISTENPORT=8081 docker.redpanda.com/redpandadata/console:latest`
