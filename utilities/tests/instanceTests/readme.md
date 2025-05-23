# Tests for Single Host Deployment

To run the tests:

`./mvnw verify`

Configue the run with a `config.json` in this directory:

```yaml
instance:
  hostname: "<ip or host>"
  clientId: test-client
  clientSecret: <secret>
  database: "test"
cleanupAfter: false
```

Also add the test client to the running instance:

```json
{
    "clientName": "test-client",
    "displayName": "Test Client",
    "description": "Client for test purposes",
    "roles": {}
}
```

After adding this client file, run the following to get the secret (and add it to your config):

`sudo oqm-config -g infra.keycloak.clientSecrets.test-client`


## Good commands to remember:

`sudo apt remove --purge oqm-* && sudo rm -rf /data/oqm /data/oqm-snapshots /etc/oqm`
`watch sudo docker ps`

## TODOs:

 - Create a self mode for installing on the box its running on
 - Create step to check status over time
