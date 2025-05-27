# Open QuarterMaster on Kubernetes

This directory contains resources and guides for deploying Open QuarterMaster on Kubernetes.

## Deployment Methods

### 1. Helm Chart
The easiest way to deploy OQM on Kubernetes is using our Helm chart. See [helm/README.md](helm/README.md) for detailed instructions.

```bash
helm repo add oqm https://epic-breakfast-productions.github.io/OpenQuarterMaster/helm/
helm install oqm oqm/oqm-core
```

### 2. ArgoCD
For GitOps deployments using ArgoCD, see [argocd/README.md](argocd/README.md).

### 3. Raw YAML Manifests
For manual deployments or customization, YAML manifests are available in the `yaml/` directory.

### 4. OpenShift
OpenShift-specific configurations are available in `yaml/openshift/`.

## Prerequisites

- Kubernetes 1.21+ cluster
- kubectl configured to access your cluster
- Storage class available for persistent volumes
- (Optional) Ingress controller for external access

## Quick Start

1. Clone this repository
2. Choose your deployment method from above
3. Follow the specific instructions for your chosen method

## Architecture

OQM on Kubernetes consists of:
- **oqm-core-api**: Core inventory management API
- **oqm-core-base-station**: Web UI for inventory management
- **MongoDB**: Database for storing inventory data
- **Kafka (RedPanda)**: Message broker for event streaming
- **Keycloak**: Authentication and authorization
- **Traefik**: Reverse proxy and ingress (optional)

## Development

For development setup and testing, see [docs/development.md](docs/development.md).

## Support

For issues or questions:
- Check our [documentation](../../README.md)
- Open an issue on [GitHub](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/issues)
- Join our [Discord](https://discord.gg/cpcVh6SyNn)