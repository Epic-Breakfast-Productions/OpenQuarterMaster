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
   quarkus:
     keycloak:
       devservices:
         port: 9328
         realm-name: oqm
         users:
           alice: alice
           bob: bob
         roles:
         # roles listed in https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/rest/auth/roles/Roles.java
           alice:
            - user
            - inventoryAdmin
            - inventoryView
            - inventoryEdit
            - itemCheckout
   oqm:
     core:
       api:
         baseUri:
   ```

## TODOs:

- Add more endpoints
- Add method to supply import data for devservice

## Deployment

https://central.sonatype.org/publish/publish-portal-maven/

`./mvnw deploy`

then:

https://central.sonatype.com/publishing/deployments


