# Single Host Architecture

This is a guide of how the single host deployment method is architected.

On a whole, OQM is made up via a collection of services. These services are run within Docker containers, and configured
to work together in this environment.

The Single Node Host setup is made to be self-reliant, only reaching out to pull docker containers and updates.

[TOC]

## Services

The services we deploy are in several categories;

### Types

#### Core

These are the core components that are always to be installed as part of the basic OQM system.

#### Infrastructure

Infrastructure components are services that we don't maintain, but we package with the system to facilitate the rest of the services.

These services can be found in the [Infrastructure](../Infrastructure) directory.

#### Plugins

Plugins are the services that build upon the core functionality to tailor the user experience. The plugins available for install can be found through the [Station Captain](../Station-Captain) tui, and the codebases for plugins can be found [here](../../../software/plugins).

Please [reach out](../../../CONTRIBUTING.md) if you have ideas for plugins (or really of any kind). A list of proposed/ planned plugins are available [here](../../../software/plugins/docs/NewPluginIdeas.md)

### Networking

The networking of this system is facilitated by Docker, and the [traefik infrastructure component](../Infrastructure/traefik).

All services are run directly on the container network `oqm-internal`. This keeps all services close and reachable to each other, but unavailable to the outside world by default. Outside is then provided through the [traefik reverse proxy.](../Infrastructure/traefik).

Some services could be directly exposed to the net for specific use cases, but it is recommended to keep things going through the proxy.

## Directories

Listed here are the directories the system uses to facilitate running:

| Directory                | Purpose                                                                                                                                      |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| /var/log/oqm/            | The location where non-service logs go to. Service logs go to journalctl. The logs here are from script utilities such as `oqm-captain`      |
| /etc/oqm/                | The main resource directory for OQM. Many utilities access or modify contents in this directory. Sub directories listed below.               |
| /etc/oqm/accountScripts/ | The location of scripts provided by services to ensure accounts exist for those services. Examples being postgres and mongodb                |
| /etc/oqm/certs/          | The default location of certs.                                                                                                               |
| /etc/oqm/config/         | Files related to the `oqm-config` utility. More information available [here](config.md)                                                      |
| /etc/oqm/kcClients/      | Files describing keycloak clients to keycloak. More information available [here](../Infrastructure/keycloak)                                 |
| /etc/oqm/proxyConfig.d/  | Files used to configure the nginx proxy. More information available [here](../Infrastructure/nginx)                                          |
| /etc/oqm/serviceConfig/  | Files for particular services to configure themselves. A mix of templates and full files used to set services up.                            |
| /etc/oqm/snapshots/      | Scripts for facilitating taking snapshots. Intended to be the utilities specific to move the appropriate files during a snapshot or restore. |
| /etc/oqm/ui.d/           | Files for relaying to [Depot](../../../software/core/oqm-depot) what front-end ui's exist to interact with. More info in that service.            |

## Management

### Logging

### Configuration

Configuration of these services should be done 