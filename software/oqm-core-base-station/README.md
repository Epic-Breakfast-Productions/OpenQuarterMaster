# oqm-core-base-station

![Docker Image Version](https://img.shields.io/docker/v/ebprod/oqm-core-base-station?label=Docker%20Image)

The Base Station is the web-based user interface for Open QuarterMaster, providing an intuitive frontend for inventory management operations.

## Overview

OQM Base Station is a Quarkus-based web application that serves as the primary user interface for interacting with the OQM Core API. It provides:

- **Inventory Management**: Add, edit, and track items in your inventory
- **Storage Organization**: Manage storage locations and organize items
- **Search & Filtering**: Quickly find items with powerful search capabilities
- **User Management**: Handle user accounts and permissions (when using external auth)
- **Plugin Integration**: Access additional functionality provided by installed plugins
- **Responsive Design**: Works on desktop and mobile devices

## Prerequisites

- Java 21 (OpenJDK or Oracle JDK)
- Gradle 8.5+
- Running instance of [oqm-core-api](../oqm-core-api)
- (Optional) Docker for containerized deployment

## Quick Start

### Development Mode

Run the application with hot-reload enabled:

```bash
./gradlew quarkusDev
```

The application will be available at http://localhost:8081

> **Note**: The Quarkus Dev UI is available at http://localhost:8081/q/dev/

### Configuration

Key configuration properties (set via environment variables or `application.yml`):

```yaml
# Core API connection
service.coreApi.url: http://localhost:8080

# Authentication mode
service.authMode: EXTERNAL  # or SELF

# External auth (Keycloak)
service.externalAuth.url: http://localhost:8090
service.externalAuth.realm: oqm
service.externalAuth.clientId: oqm-base-station
```

See the [full configuration guide](docs/README.md) for all available options.

## Building

### JVM Build

```bash
./gradlew build
```

### Native Build

```bash
./gradlew build -Dquarkus.native.enabled=true
```

### Docker Image

```bash
docker build -f src/main/docker/Dockerfile.jvm -t oqm-core-base-station:latest .
```

### System Packages

Create DEB/RPM packages:

```bash
./makeInstallers.sh
```

## Features

- **Item Management**: Create, update, and track inventory items with detailed metadata
- **Barcode Support**: Scan barcodes to quickly add or find items
- **Image Management**: Attach images to items and storage locations
- **Expiry Tracking**: Monitor expiration dates and get notifications
- **History Tracking**: View complete history of item movements and changes
- **Export/Import**: Export inventory data in various formats
- **Custom Attributes**: Add custom fields to items for your specific needs

## Architecture

Base Station is built with:
- **Quarkus**: Supersonic Subatomic Java Framework
- **Qute**: Type-safe templating engine
- **RESTEasy Reactive**: Reactive REST client for API communication
- **OpenID Connect**: For authentication with Keycloak
- **Bootstrap**: Responsive UI framework

## Documentation

- [Development Guide](docs/README.md)
- [User Guide](docs/User Guide.adoc)
- [Authentication Setup](docs/usersAndAuth.md)
- [Main Project Documentation](../../README.md)

## TODOs

- Handle case when OQM database is not present
- Implement database availability utility
- Add offline mode support
- Improve mobile UI experience

## Support

- [GitHub Issues](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/issues)
- [Discord Community](https://discord.gg/cpcVh6SyNn)
- [Project Website](https://openquartermaster.com)