#######################################
Design Decisions
#######################################

This document is intended to outline the why and how of our decisions regarding the way we have built out the service.

Operational / Architecture Decisions
====================================

This section talks to how we arrived at the decisions surrounding how we have built the service, and
why we chose the tools we did.

Quarkus / Java
--------------

`Quarkus <https://quarkus.io>`_ is a fully featured, lean, and performant framework for building many things, but most notably web, container-native services.

Quarkus is Java-based, and thus is the language we are working in. Java was chosen as is a good choice for such services, being a strongly typed language with a mature
framework ecosystem.

Other options could include Spring, or things in other

Container Deployment
--------------------

`Containers <https://www.docker.com/resources/what-container/>`_ provide an extreme level of flexibility and ease of deployment in a variety of environments.

If it can run Podman/ Docker, it can run the core API.

MongoDB
-------

`MongoDB <https://www.mongodb.com/>`_ Is a highly flexible and scalable database. We chose this over \*.sql to leverage:

 * flexibility in data model; can directly use polymorphic objects within the same collection
 * scalability; naturally a clustered system, can grow with OQM as a cluster

JWT Auth
--------

`JWTs <https://www.jwt.io/introduction#what-is-json-web-token>`_ are a flexible, implementation independent method for authorization. Being a widely
used web standard, was a natural choice

Kafka Messaging
---------------

`Apache Kafka <https://kafka.apache.org/>`_ is a widely used and supported messaging platform. What made it stand out was a relative level of simplicity,
both in usage and in deployment.

Optionality
^^^^^^^^^^^

It is important to the project to be flexible and modular, and Kafka frankly eats up a lot of computing resources. By making the Core API only optionally
include connection to a Kafka server, we can omit that overhead when we don't need to include it when the downstream functionality is unused.

OpenTelemetry
-------------

`OpenTelemetry <https://opentelemetry.io/>`_ is a widely used standard for transferring metrics, logs, and tracing information to tools that can use that information.
It made sense to adopt, as is largely vendor agnostic.

Optionality
^^^^^^^^^^^

As with Kafka, if users don't want the overhead of a metrics stack, the should not need to provide and use one.

Functional / Design Decisions (major)
=====================================

This section goes over why we implemented certain high level functionalities the way we did.
We won't go too deep into every tiny design choice, but will talk to higher level design patterns.
