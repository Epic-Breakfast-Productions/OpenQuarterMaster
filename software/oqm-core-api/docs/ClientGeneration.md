# Client Generation

`docker run --rm -v $PWD/generatedClients:/local openapitools/openapi-generator-cli generate -i /local/openapi.yaml -g jaxrs-cxf-client --library quarkus -o /local/out/quarkus`

