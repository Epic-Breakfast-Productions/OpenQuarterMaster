Prerequisites
=============

Required
--------

System
^^^^^^

* CPU: ARM or x64
* Memory: ~500Mb reasonable minimum, depending
* Docker / Podman / Container Runtime

MongoDB
^^^^^^^

Mongodb is the backend database for the core api.

JWT Provider / Token
^^^^^^^^^^^^^^^^^^^^

The JWT needs to be granted by an additional authority. Example being Keycloak.

Optional
--------

These are optional setups that can be tied into the system.

Kafka Instance
^^^^^^^^^^^^^^

The core api is capable of optionally connecting to a Kafka instance to leverage message-based patterns.

OpenTelemetry
^^^^^^^^^^^^^^

You can have the core api pass on metrics, traces, logs to an OpenTelemetry endpoint.

SSL Cert
^^^^^^^^

You can optionally configure a SSL cert to enable HTTPS. You will have to have this provided.
