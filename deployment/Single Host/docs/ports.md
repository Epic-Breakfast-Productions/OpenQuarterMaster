# SNH Ports

This document outlines what ports are used for the system on a Single Node Host deployment.

## Core

Core components generally occupy the port range `9000`-`9254`. Known ports listed below.

| Port | Component                                                         | Purpose                            | Forwarded to host |
|------|-------------------------------------------------------------------|------------------------------------|-------------------|
| 80   | [Depot](../../../software/oqm-depot/README.md)                    | Depot HTTP connection port         | Yes               |
| 443  | [Depot](../../../software/oqm-depot/README.md)                    | Depot HTTPS connection port        | Yes               |
| 9000 | [Core API](../../../software/oqm-core-api/README.md)              | Core API HTTP connection port      | Yes               |
| 9001 | [Core API](../../../software/oqm-core-api/README.md)              | Core API HTTPS connection port     | Yes               |
| 9005 | [Base Station](../../../software/oqm-core-base-station/README.md) | Base Station HTTP connection port  | Yes               |
| 9006 | [Base Station](../../../software/oqm-core-base-station/README.md) | Base Station HTTPS connection port | Yes               |

## Infrastructure

Infrastructure components generally occupy the port range `8090`-`8999`. For certain services with well-established port numbers, those are used instead. Known ports listed below.

| Port  | Infra Service | Purpose                 | Forwarded |
|-------|---------------|-------------------------|-----------|
| 27017 | Mongo         | Mongodb connection port | Optional  |
| 8090  | Jaeger        | Jaeger UI               | Optional  |
| 8091  | Jaeger        | Jaeger collector        | Optional  |
| 8092  | Jaeger        | OTLP gRPC port          | Optional  |
| 8095  | Prometheus    | Prometheus UI           | Optional  |
| 8100  | OpenTelemetry | OpenTelemetry?          | Optional  |
| 8105  | Apache Kafka  | Kafka connection        | Optional  |
| 8110  | Grafana       | Grafana UI port         | Optional  |
| 8115  | Keycloak      | Keycloak port           | Yes       |
| 8120  | Postgres      | Postgres port           | Optional  |

## Plugins

Plugin components generally occupy the port range `9255`-`9999`. These will be managed by [Station Captain](../Station-Captain/README.md), and thus don't have predefined values. They do, however, have consistent ports for your install.


