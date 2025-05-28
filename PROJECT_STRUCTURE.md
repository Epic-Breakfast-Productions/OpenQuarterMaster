# OpenQuarterMaster Project Structure

This document provides an overview of the OpenQuarterMaster repository structure and the purpose of each major directory.

## Repository Layout

```
OpenQuarterMaster/
├── deployment/         # Deployment configurations and scripts
├── hardware/          # Hardware integration (Arduino, MSS)
├── media/            # Project media assets (logos, diagrams)
├── software/         # All software components
├── utilities/        # Build and maintenance utilities
└── docs/            # Project-wide documentation
```

## Detailed Structure

### `/deployment` - Deployment Configurations

Contains deployment methods for different environments:

- **`/Single Host`** - Scripts and configs for single-server deployments
  - `Infrastructure/` - Infrastructure services (MongoDB, Kafka, Keycloak, etc.)
  - `Station-Captain/` - Deployment management tool
  - `metrics/` - Monitoring stack (Prometheus, Grafana, Jaeger)
- **`/Kubernetes`** - Kubernetes deployment resources
  - `helm/` - Helm charts
  - `argocd/` - ArgoCD configurations
  - `yaml/` - Raw Kubernetes manifests
- **`/Compose`** - Docker Compose configurations

### `/software` - Software Components

Core software components of OpenQuarterMaster:

#### Core Components
- **`/oqm-core-api`** - Main REST API server (Java 21, Quarkus)
  - Handles inventory management logic
  - Provides REST endpoints
  - Manages database operations
- **`/oqm-core-base-station`** - Web UI frontend (Java 21, Quarkus)
  - User interface for inventory management
  - Connects to core-api

#### Libraries
- **`/libs`** - Shared libraries
  - `open-qm-core/` - (DEPRECATED) Legacy core library
  - `open-qm-moduleDriver/` - Hardware module driver library
  - `core-api-lib-quarkus/` - Quarkus extension library

#### Plugins
- **`/plugins`** - Extension plugins
  - `external-item-search/` - External product search integration
  - `image-search/` - Image-based item search
  - `alert-messenger/` - Alert and notification system
  - `mss-controller/` - Modular Storage System controller
  - `open-qm-plugin-demo/` - Demo plugin template

#### Drivers
- **`/drivers`** - Hardware drivers
  - `demoDriver/` - Example hardware driver
  - `open-qm-driver-server/` - Driver server implementation

### `/hardware` - Hardware Integration

Hardware designs and firmware:

- **`/mss`** - Modular Storage System
  - `mss/` - Arduino firmware
  - `jsonSchemas/` - Communication protocol schemas
  - `schematics/` - Hardware schematics (KiCad)

### `/media` - Media Assets

Project media resources:

- **`/logo`** - OpenQuarterMaster logos and branding
- **`/diagrams`** - Architecture and system diagrams

### `/utilities` - Build and Maintenance Tools

- **`/tests`** - Integration and system tests
- Build scripts and repository maintenance tools


## Key Files

- `README.md` - Main project documentation
- `LICENSE` - Apache 2.0 license
- `SECURITY.md` - Security policy
- `CONTRIBUTING.md` - Contribution guidelines
- `CODE_OF_CONDUCT.md` - Community standards

## Development Workflow

1. **Core Development**: Work primarily in `/software/oqm-core-*` directories
2. **Plugin Development**: Create new plugins in `/software/plugins/`
3. **Deployment Testing**: Use `/deployment/Single Host/` for local testing
4. **Documentation**: Update relevant README files in each component

## Naming Conventions

- Service names: `oqm-*` (e.g., oqm-core-api, oqm-core-base-station)
- Java packages: `tech.ebp.oqm.*`
- Docker images: `ebprod/oqm-*`
- System packages: `oqm-*_version_all.deb`

## Getting Started

For development setup, see:
- [Development Getting Started](software/oqm-core-api/docs/development/DevelopmentGettingStarted.md)
- [Single Host Deployment](deployment/Single Host/README.md)
- [Main README](README.md)