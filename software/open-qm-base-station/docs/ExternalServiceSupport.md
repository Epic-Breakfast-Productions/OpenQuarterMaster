# External Service Support

[Back](README.md)


This documents how external services interact with the base station.


Reasons to treat external services in this manner:

 - Provide a method for registering base station components to interact with the external service (See plugins)
 - In `SELF` auth mode, allow for authentication of these external services.

## Overview

Generally speaking there are two external service specific endpoints:

```
/api/externalService/setup/self
/api/externalService/setup/external
/api/externalService/auth
```

```mermaid
graph TD;
    A-->B;
    A-->C;
    B-->D;
    C-->D;
```