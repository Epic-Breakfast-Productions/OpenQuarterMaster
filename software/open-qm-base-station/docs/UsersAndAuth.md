# Users and Authentication

[Back](README.md)

This guide explains how users are authenticated in this system.

- https://auth0.com/docs/authorization/flows/authorization-code-flow-with-proof-key-for-code-exchange-pkce

## Authentication

The endpoints on this server are protected by RBAC controls, utilizing JWT's.

If the config value `service.authMode` is set to `SELF`, then the service itself will provide those tokens and user
management.

If the config value is `EXTERNAL`, then the service assumes an external credential provider is at play and won't perform
user management.

### JWT

- https://quarkus.io/guides/security-keycloak-authorization
- https://quarkus.io/guides/security-openid-connect-web-authentication
- https://quarkus.io/guides/security-oauth2

#### Claims

The following claims are considered in this application:

|   Claim name | Short for         | TL;DR/ Description/ Data description                                                             | Usage in SELF auth mode | Usage in EXTERNAL auth mode                       |
|-------------:|-------------------|:-------------------------------------------------------------------------------------------------|:------------------------|:--------------------------------------------------|
|          sub | Subject           | Subject of JWT, is the user's id String                                                          | -                       | Used as the value for `User.external ids`         |
|          iss | Issuer            | The issuer of the JWT, The entity that issued the token.                                         | -                       | Used as the key for `User.external ids`           |
|          upn | userPrincipalName | The user's sign-in name. Username.                                                               | -                       | Used as the value for                             |
|        email |                   | The user's email.                                                                                | -                       | Used as the user's email in the internal db.      |
|        title |                   | The user's title.                                                                                | -                       | Used as the user's title in the internal db.      |
|   given_name |                   | The user's given/ first name.                                                                    | -                       | Used as the user's first name in the internal db. |
|  family_name |                   | The user's family/ last name.                                                                    | -                       | Used as the user's last name in the internal db.  |
| roleMappings |                   | Unused                                                                                           | -                       | -                                                 |
|       groups |                   | The roles for what the user should be allowed to do                                              | -                       | -                                                 |
|          aud | audience          | The audience for the token; Recipient for which the JWT is intended. User identification string. | -                       | -                                                 |
|    auth_time |                   | The epoch time of when the authorization happened                                                | -                       | -                                                 |
|          exp | expiration time   | The epoch time of when the token will expire                                                     | -                       | -                                                 |
|          jti | JWT ID            | Unique ID of the token, allows revocation of the token or for it to only be used once.           | -                       |                                                   |

References:

- https://auth0.com/docs/security/tokens/json-web-tokens/json-web-token-claims

### For External Auth

For the service to work with `service.authMode` set to `EXTERNAL`:

- service needs the `mp.jwt.verify.publickey` set to the public key cert from the issuer of the jwt tokens
- Tokens need to be provided with all claims listed above

## User Roles

| Role          | Description                                                                            |
|---------------|:---------------------------------------------------------------------------------------|
| user          | Given to everyone; required for any access.                                            |
| userAdmin     | Required to look up or modify users beyond one's own. Given to the first created user. |
| inventoryView | Required to view inventory related resources; Items, Storage Blocks, Images.           |
| inventoryEdit | Required to make edits to inventory related resources; Items, Storage Blocks, Images.  |
