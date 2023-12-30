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
