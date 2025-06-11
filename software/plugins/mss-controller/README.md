# mss-controller-plugin


`mvn help:evaluate -Dexpression=project.version -q -DforceStdout`




`./gradlew clean build -Pquarkus.container-image.build=true -Pquarkus.jib.platforms=linux/arm64,linux/amd64 -Pquarkus.container-image.group=ebprod -Pquarkus.container-image.name=oqm-plugin-mss-controller -Pquarkus.container-image.push=true`

This plugin interacts with [MSS Modules](../../../hardware/mss), bringing their functionality into the OQM system.

Currently a proof of concept, much duct tape in certain areas holding things together.

https://github.com/openai/whisper


 - https://medium.com/@mihirrajdixit/getting-started-with-service-accounts-in-keycloak-c8f6798a0675
 - https://github.com/LAION-AI/Open-Assistant


## Speech to Text

 - Java:
   - https://www.geeksforgeeks.org/converting-text-speech-java/#
   - https://developer.ibm.com/tutorials/embed-speech-to-text-functions-into-your-java-application/
   - https://cmusphinx.github.io/wiki/tutorialsphinx4/
 - Other:
   - http://voice2json.org
     - http://voice2json.org/install.html#docker-image
     - http://voice2json.org/commands.html#download-profile
     - https://shisho.dev/blog/posts/docker-in-docker/
     - http://voice2json.org/sentences.html
     - http://voice2json.org/recipes.html


## voice2json notes

 - Probably best way to run is docker, docker-in-docker? Most portable

### Steps:


- needs {v2jhome} to be populated with ".local/share/"
- using the bash script `voice2jsonTest`, which runs the utility in docker

 1. `voice2jsonTest -p en download-profile`
 1. Modify content in the () `sentenctes.ini`, `slot_programs/`, `slots/`
    - `sentences.ini` - the main setup for intents. references `slots/`, `slot_programs/` dirs
    - `slot_programs/` - programs that generate values for slots go in this dir
      - http://voice2json.org/sentences.html#slot-programs
 1. Train the profile using: `voice2json train-profile`
    - Can be run as needed or updated. 

















This project uses Quarkus. See [Quarkus Basics](../../docs/QuarkusBasics.md) for build and run instructions.

## Related Guides

- REST Client Reactive ([guide](https://quarkus.io/guides/rest-client-reactive)): Call REST services reactively
- Qute ([guide](https://quarkus.io/guides/qute)): Offer templating support for web, email, etc in a build time, type-safe way
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- OpenID Connect ([guide](https://quarkus.io/guides/security-openid-connect)): Verify Bearer access tokens and authenticate users with Authorization Code Flow
- Scheduler ([guide](https://quarkus.io/guides/scheduler)): Schedule jobs and tasks
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health
- Micrometer metrics ([guide](https://quarkus.io/guides/micrometer)): Instrument the runtime and your application with dimensional metrics using Micrometer.

## Provided Code

### RESTEasy Reactive

Easily start your Reactive RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### RESTEasy Reactive Qute

Create your web page using Quarkus RESTEasy Reactive & Qute

[Related guide section...](https://quarkus.io/guides/qute#type-safe-templates)

### SmallRye Health

Monitor your application's health using SmallRye Health

[Related guide section...](https://quarkus.io/guides/smallrye-health)
