# SNH Networking

[Back](README.md)

This document outlines how networking is handled in the Single Node Host system.

The Single Node Host system employs
a [Traefik](https://traefik.io/) [reverse proxy](https://en.wikipedia.org/wiki/Reverse_proxy) using a path-based scheme
in order to:

- simplify access
- be flexible to deployment environment
- increase security

Traefik runs within a container on the system, and is the only directly accessible service in the system by default.
Services such as Depot, Base Station, Core API, etc are accessed via paths served by the proxy. Services are organized by their type (`infra`, `core`, `plugin`).

For example, by default, Depot is available under `https://<hostname/ip>/core/depot`.

Documentation on the certs used by the reverse proxy can be found [here](Certs.md).

Further documentation on our packaged Traefik instance can be found [here](../Infrastructure/traefik)
