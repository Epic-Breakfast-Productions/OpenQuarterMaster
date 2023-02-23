# Releasing

[Back](README.md)

## Prereqs:

1. Docker, with buildkit enabled
   - https://www.howtogeek.com/devops/what-is-dockers-buildkit-and-why-does-it-matter/
   - https://docs.docker.com/build/buildx/install/
   - https://github.com/docker/buildx/issues/132
   - Steps:
     1. Setup buildx
        1. TODO::
     2. Make a builder `docker buildx create --name mybuilder --use`

## Release Steps

These are the steps to take to perform a release of the software:

1. If it's been a while...
   1. review Dockerfiles for base image version
   2. review dependencies for version updates
2. Run _all_ tests:
    1. `./gradlew clean test`
    2. If it's been a while, review Dockerfiles for base image version
3. Increment version of service accordingly in `build.gradle`
4. Ensure everything committed and pushed to github. Check workflows.
5. Be logged into docker hub with ebprod user `docker login`
6. Deploy jvm version
   1. Clean/build/push project `./gradlew clean build -Pquarkus.container-image.build=true -Pquarkus.jib.platforms=linux/arm64,linux/amd64 -Pquarkus.container-image.group=ebprod -Pquarkus.container-image.push=true`
   2. Ensure was deployed successfully: https://hub.docker.com/repository/registry-1.docker.io/ebprod/open-qm-base-station/tags?page=1&ordering=last_updated
7. Make installers: `./makeInstallers.sh`
8. Make release for version on Github, attach all installers to it (`build/installers`)