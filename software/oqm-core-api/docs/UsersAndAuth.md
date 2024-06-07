# Users and Authentication

[Back](README.md)

This guide explains how users are authenticated in this system component.

In short, you have two options;

 - JWT- based RBAC (preferred, standard)
 - Basic Auth (NOT preferred, for testing and simple, secure environment usecases only)

## JWT

As stated, [JWT](https://jwt.io/) based auth is the standard and preferred method for authenticating with this service.
JWT's provide simplicity and security in a distributed software model. In our ecosystem, when accessing the web ui's, those ui services sign users into the [OIDC](https://openid.net/developers/how-connect-works/) provider (in the standard single-node deployment, that is [Keycloak](../../../deployment/Single%20Host/Infrastructure/keycloak)). This gives the uis a jwt, which is then passed onto this backend service to perform actions.

### Configuration & References

All configuration options for the JWT setup can be found in Quarkus' documentation:

 - Quarkus JWT plugin guide: https://quarkus.io/guides/security-jwt
   - Config reference for that plugin: https://quarkus.io/guides/security-jwt#configuration-reference
 - Single node host jwt configuration (Under "jwt verification"): [core-api-config.list](../installerSrc/core-api-config.list)

### Calling an endpoint with JWT

Providing a JWT is easy. Simply provide the `Authorization` header with the value: `Bearer <token>`

Below is an example with curl:

```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/inventory/item' \
  -H 'accept: application/json' \
  -H 'Authorization: Bearer ${TOKEN}'
```

## Basic

Basic auth is, well, pretty basic and therefore insecure. It consists of plaintext credentials sent in a base64 encoded string.

> [!WARNING]  
> We **_strongly_** recommend not using basic authentication in production or public-facing situations.
> It is not secure or sustainable in a real working environment.
> We support this feature because:
>  1. It is easy to provide, optionally based on configuration (default is not to activate)
>  2. Makes development easier, as we use frameworkk tooling to abstract out authentication methods.
>  3. We can visualize edge cases where it could be useful

### Configuration & References

 - Mozilla docs on basic auth: https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#basic_authentication_scheme
 - Quarkus docs:
   - https://quarkus.io/guides/security-basic-authentication-howto

Configuration example:

```yaml
quarkus:
  smallrye-jwt:
     enabled: false
  security:
    users:
      embedded:
        enabled: true
        plain-text: true
        users:
          joe: "password"
          adminBob: "password"
        roles:
          joe: "inventoryView,inventoryEdit"
          adminBob: "inventoryView,inventoryEdit,inventoryAdmin"
```

### Calling an endpoint with Basic

```bash
TOKEN=$(base64 "user:pass")
curl -X 'GET' \
  'http://localhost:8080/api/v1/inventory/item' \
  -H 'accept: application/json' \
  -H 'Authorization: Basic ${TOKEN}'
```
