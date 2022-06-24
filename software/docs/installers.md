# Software Installers

## Station Master

The station master is meant as the main setup and management tool for standalone installations not on some form of orchestration framework such as Kubernetes. It is meant to be setup first and facilitates the install of the rest of the system.

More information in the [Station Captain](../Station%20Captain/) directory.

## Service Setup

### Locations used

These configurations listed here are meant to be updated and adjusted by the installers, and where noted, mapped to the actual services running in containers.

#### Configs

Configuration in this context means configuration passed to each service relaying configuration relevant to more than one of the services, and not any one service's configuration.

The main Configuration directory is:
`/etc/oqm/`

This is mapped directly to service containers at the same path.

Plugin configuration is located `/etc/oqm/plugins/`

#### Logs

`/var/log/oqm/`, `/var/log/oqm/{service}/*`

Each service is given its own directory to log to, mapped to the same path in the container.

#### Temporary files

`/tmp/oqm/` (when needed; containerized services will use their own temp dirs)

#### Other

##### Mongodb files

Mongodb files are mapped from the mongodb container to `/data/db/oqm_mongo/`, for safe keeping between potential issues with container upgrades, etc.
That being said, one should still backup files periodically by utilizing `mongodump`, and connecting to the mongo instance directly (to be automatically handled by the station captain script). 

### Service Ports

In general, the base station will take port `80`(and/or `443`) as the default web interface.
All other services will be given a port on startup, and the shared configuration to be updated accordingly.

Jaeger is given ports of `8090` and `8091`, Mongo set to the default port of `27017`