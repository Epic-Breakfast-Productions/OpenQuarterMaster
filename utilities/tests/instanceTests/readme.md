# Tests for Single Host Deployment

To run the tests:

`./mvnw verify`

Configue the run with a `config.json` in this directory:

```yaml
setupConfig:
  type: "EXISTING"
  host: oqm-install-test.local
  user: gstewart
  password: 1!Letmein
  installTypeConfig:
    type: "REPO"
#    repoBranch: "test-dev"
cleanupAfter: true 
```

## Good commands to remember:

`sudo apt remove --purge oqm-* && sudo rm -rf /data/oqm /data/oqm-snapshots /etc/oqm`
`watch sudo docker ps`

## TODOs:

 - Create a self mode for installing on the box its running on
 - Create step to check status over time
