# Keycloak OQM Infra Component

This postgresql instance is currently used by all services that need OIDC and token access. Examples:

 - [Base Station](../../open-qm-base-station)
 - [Plugins](../../plugins)

In case you get the "HTTPS is required" error: 

```
bash-5.1$ cd /opt/keycloak/bin/
bash-5.1$ ./kcadm.sh config credentials --server http://localhost:8080/ --realm master --user oqmAdmin
bash-5.1$ ./kcadm.sh update realms/master -s sslRequired=NONE
bash-5.1$ ./kcadm.sh update realms/oqm -s sslRequired=NONE
```
Run on the container where keycloak is running. This will be obsolete once we get a unified https model.


## Resources

 - https://www.keycloak.org/server/db
   - https://www.keycloak.org/server/db#_relevant_options
 - https://www.keycloak.org/server/containers
   - https://www.keycloak.org/server/containers#_importing_a_realm_on_startup
 - https://www.keycloak.org/server/all-config
 - https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/