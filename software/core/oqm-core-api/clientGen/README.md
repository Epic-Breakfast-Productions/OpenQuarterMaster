# Generating Clients for OQM Core API

## Prerequisites

 1. Docker
 2. A Downloaded `openapi.yaml` from the core api; available at `/q/openapi/`. Place file in this directory.

All commands listed to be run in this directory


## Running

```bash
docker run --rm \
-v ${PWD}:/local \
openapitools/openapi-generator-cli generate \
-i /local/openapi.yaml -g jaxrs-cxf-client -o /local/out/jaxrs -c /local/jaxrs-cxf-client-config.json
```

# References

 - https://openapi-generator.tech
   - https://openapi-generator.tech/docs/installation#docker
   - Generators:
     - https://openapi-generator.tech/docs/generators/java
     - https://openapi-generator.tech/docs/generators/jaxrs-cxf-client

