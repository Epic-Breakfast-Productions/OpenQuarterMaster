# OQM via Compose

This directory contains the resources needed to run the OQM system via Podman/Docker compose.

The scope at the moment is to provide a basic featureset, i.e, just the core features.

> [!CAUTION]
> Important considerations:
> - This is a less secure setup:
>   - We don't provide configuration management
>   - No SSL certs (https)
>   - Services are directly exposed on individual ports, and the Base station is on the host network.
>   - No realm management or host recognition for Keycloak (anybody can connect and manage, use the client, etc)
> - Remember to treat the `.env.example` as a TEMPLATE. There are default values for passwords that you should update.
> - This is a barebones setup; just the core utilities. No plugins or utilities included.

Use cases:

 - _**Local**_ usage and testing
 - Non-"prod" workloads where you don't need extras


> [!TIP]
> For a more robust, and supported setup, please look  at [single host deployment](../Single%20Host/).

If you want more features added or improvements to this, please submit an issue, hit us up in Discord or discussions, or submit a PR.

## Usage

Run these commands in the [compose](compose) directory.

> [!NOTE]
> These commands use the Ubuntu convention of `docker-compose` for the compose command. However, you might be using something different, and might need `docker compose` or even `podman compose` as is relevant to your system's docker setup.


To run the compose file, you can run:

```bash
docker-compose up
```

To run it in the background:

```bash
docker-compose up -d
```

The containers are run with `unless-stopped` as a restart policy, so they will restart automatically if they stop, or the system restarted.

To stop them and remove the containers:

```bash
docker-compose down
```
