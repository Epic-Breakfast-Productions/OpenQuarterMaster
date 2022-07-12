# Versioning, Branching, Tagging

[Back](README.md)

## Versioning

The software in this repository follows [Semantic Versioning](https://semver.org/):

`{MAJOR}.{MINOR}.{PATCH}[-{LABEL}]`

> Given a version number MAJOR.MINOR.PATCH, increment the:
> 
>   - MAJOR version when you make incompatible API changes,
>   - MINOR version when you add functionality in a backwards compatible manner, and
>   - PATCH version when you make backwards compatible bug fixes.
> 
> Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.

The labels we typically use are below:

- `FINAL`, to denote a particular version is the last of the major/minor version number.
- `DEV`, to denote the particular version is in development; don't use 'for real'

## Branching Strategy

This project will adhere to the following branch strategy:

- `master`
  - `v{MAJOR}`
    - `v{MAJOR}.lib`
    - `v{MAJOR}.base`
    - `v{MAJOR}.plugin`
    - `v{MAJOR}.env`

## Releases

Each piece of software here will get it's own release tag scheme. Generally speaking, each will follow the following format: `{TYPE}-{PROJECT}-{VERSION}`, where "type" refers to what kind of project it is.

Example for each project:

 - Base Station - `Core-Base_Station-1.0.0-DEV`
 - Jaeger - `Infra-Jaeger-1.0.0-DEV`
 - Mongo - `Infra-Mongo-1.0.0-DEV`
 - Station Captain - `Manager-Station_Captain-1.0.0-DEV`
