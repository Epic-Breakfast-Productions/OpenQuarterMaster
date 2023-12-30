# Tests for Single Host Deployment

To run the tests:

`./mvnw verify`

Configue the run with a `config.json` in this directory:

```json
{
	"setupConfig":{
		"type": "EXISTING",
		"host": "oqm-install-test.local",
		"user": "",
		"password": ""
	}
}
```
