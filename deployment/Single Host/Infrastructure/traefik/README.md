# Traefik reverse Proxy OQM Infra Component

[Back](../README.md)

This infrastructure component is a reverse proxy to handle access to the various services presented by OQM. This setup
is designed to provide path-based proxying.

[Traefik](https://doc.traefik.io/traefik/) is a prupose-built reverse proxy server that is lightweight, powerful, and
easy to configure.

## Config

Below is a table of applicable configuration values.

TLS cert configuration information can be found [here](../../docs/Certs.md)

More information on dealing with configuration can be found [here](../../docs/config.md)

| Config Key                            | Default | Explanation                                                                                                    |
|---------------------------------------|---------|----------------------------------------------------------------------------------------------------------------|
| `infra.traefik.httpPort`              | `80`    | The default port for traefik to listen to HTTP traffick on. This traffic is automatically redirected to HTTPS. |
| `infra.traefik.httpsPort`             | `443`   | The default port for traefik to listen to HTTPS traffick on.                                                   |
| `infra.traefik.managementPortEnabled` | `false` | Enables the Traefik management interface at port `8080`. Use to view and debug Traefik configuration.          |

## Overview

This setup automatically sets up Traefik configuration to forward traffic to services. This setup is informed by files
services place in a directory for this to pickup and generate the Traefik dynamic configuration.

The directory that services want to be proxied should place their config into:

`/etc/oqm/proxyConfig.d/`

The schema of the configuration:

```json
{
	"type": "<type>",
	// The "type" of service. "core", "infra", "plugin", "metrics"
	"name": "<name>",
	// The actual name of the service
	"internalBaseUriConfig": "core.api.internalBaseUri",
	// The config value of where requests are proxied to
	"preservePath": false,
	// If traefik should include prefic information in the x-forwarded headers
	"stripPrefixes": true
	// If traefik should strip the path prefix before calling the service.
}
```

The resulting path that the service is available is `https://<host>:<https port>/<type>/<name>/`
