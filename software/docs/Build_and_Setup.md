# Building and Setting Up OQM Components

This page outlines how to build the main software pieces from source and the prerequisites needed for development or custom deployments.

## Prerequisites

- **Java Development Kit (JDK) 21 or newer**.  The core services and most plugins target Java&nbsp;21 via Gradle toolchains.
- **Docker** (or Podman) for running local dependencies like MongoDB and for building container images.
- **Git** for cloning the repository.
- **Gradle** is provided via the Gradle Wrapper (`gradlew`) in each component.  No separate installation is required, but a local Gradle 8+ will also work if preferred.

## Building Each Component

Each software module contains its own Gradle Wrapper, so the commands below can be run exactly as shown.

### Core API

```bash
cd software/oqm-core-api
./gradlew build        # build the service
./gradlew quarkusDev   # run in development mode
```

This produces `build/quarkus-app/` with the runnable JAR.  See the `docs/development` directory in that module for more details.

### Base Station

```bash
cd software/oqm-core-base-station
./gradlew build
./gradlew quarkusDev
```

The Base Station provides the web interface and communicates with the Core API.

### Plugins

Plugins live under `software/plugins`.  Each has its own wrapper and can be built individually, for example:

```bash
cd software/plugins/open-qm-plugin-demo
./gradlew build
```

When deploying, plugins talk to the Core API over REST or messaging channels and are typically run in their own containers.

### Libraries

Shared libraries under `software/libs` are built the same way using their included wrappers.

## How Components Connect When Deploying

- **Core API** exposes REST endpoints and messaging topics for inventory management.
- **Base Station** is a web UI that calls the Core API for all operations.
- **Plugins** extend functionality by integrating with the Core API and optionally consuming or producing messages.
- **Station Captain** (see `deployment/Single Host`) orchestrates the services on a single host and manages supporting tools such as MongoDB, AMQ, and Jaeger.

Together these pieces form a modular system that can run locally via Docker Compose or in Kubernetes.  Consult the `deployment` directory for ready‑made manifests and further instructions.
