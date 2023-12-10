# Keycloak OQM Infra Component

This postgresql instance is currently used by all services that need OIDC and token access. Examples:

 - [Base Station](../../open-qm-base-station)
 - [Plugins](../../plugins)

The flow of this is as follows:

 - This infra provides the OQM realm
 - Individual apps provide their clients defined in:
   - `/etc/oqm/kcClient/clientName.json`
   - Defined by the following schema:
     ```json
     {
        "clientName": "",
        "roles": {
            "roleName": {
                "default": true
            }
        }
     }
     ```





In case you get the "HTTPS is required" error: 

```
bash-5.1$ cd /opt/keycloak/bin/
bash-5.1$ ./kcadm.sh config credentials --server http://localhost:8080/ --realm master --user oqmAdmin
bash-5.1$ ./kcadm.sh update realms/master -s sslRequired=NONE
bash-5.1$ ./kcadm.sh update realms/oqm -s sslRequired=NONE
```
Run on the container where keycloak is running. This will be obsolete once we get a unified https model.

## Examples:

### Turn on/off public self-serve adding of users

```bash

```

### 

```bash

```




## Resources

 - https://www.keycloak.org/server/db
   - https://www.keycloak.org/server/db#_relevant_options
 - https://www.keycloak.org/server/containers
   - https://www.keycloak.org/server/containers#_importing_a_realm_on_startup
 - https://www.keycloak.org/server/all-config
 - https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/
 - kc admin examples:
   - https://medium.com/@rishabhsvats/using-kcadm-in-keycloak-668a592691f4
   - https://gist.github.com/thomasdarimont/bb702bd1160eb200147cf1bee1c1f7ed


