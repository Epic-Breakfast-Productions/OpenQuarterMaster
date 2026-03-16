# Keycloak OQM Infra Component

This keycloak instance is currently used by all services that need OIDC and token access. Examples:

 - [Base Station](../../open-qm-base-station)
 - [Plugins](../../plugins)

## Outline

## Realm Setup, Structure

For the most part, the OQM realm is pretty straight forward. It is setup to provide the appropriate auth methodology for the system.


### Realm Roles

These roles are provided out of the box, applied at the realm level:

 - `user`
 - `extService`
 - `inventoryAdmin`
 - `inventoryView`
 - `inventoryEdit`
 - `itemCheckout`

### Groups

\* = default applied to new users

#### User *

This group is intended as a basic user.

##### Roles

 - `user`
 - `inventoryView`
 - `inventoryEdit`
 - `itemCheckout`

#### Inventory Admin

##### Roles

 - `inventoryAdmin`

#### User Admin


## Client setup

The flow of this is as follows:

 - This infra provides the OQM realm
 - Individual apps provide their clients defined in:
   - `/etc/oqm/kcClient/clientName.json`
   - Defined by the schema in the "Client Definition" section
     
     
### Service Integration Definition

```json
{
   "service": "name",
   "roles": [
      {
         "name": "",
         "description": "",
         "default": false,
         "groups": [
            "groupName"
         ],
         "realm": false
      }
   ],
   "clients": [
      {
         "clientId": "clientName",
         "displayName": "displayName",
         "description": "description",
         "roles": [ ]
      }
   ]
}
```

Explanation:

 - `service`
 - `roles` - Roles to be added to the realm
   - `roleName`
   - `default` - If this role should be a default role applied to users
   - `group` - Nullable, the group that this role should be added to
   - `realm` - Nullable, `true` or `false`. If is a realm-level role or not
 - `clients` - Clients to be added to the realm
   - `clientName` - The name of the client
   - `displayName` - The display name of the client
   - `description` - The description of the client
   - `roles` - The roles that the client has. List of strings

## Tips n Tricks


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
/opt/keycloak/bin/kcadm.sh update realms/oqm -s registrationAllowed=false
```

### Get all clients

```bash
kcadm.sh get clients -r oqm
```

### Get all roles

```bash
kcadm.sh get roles -r oqm
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


