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

## Releases

Each piece of software here will get it's own release tag scheme. Generally speaking, each will follow the following format: `{TYPE}-{PROJECT}-{VERSION}`, where "type" refers to what kind of project it is.

Example for each project:

 - Base Station - `core-base+station-1.0.0-DEV`
 - Jaeger - `infra-jaeger-1.0.0-DEV`
 - Mongo - `infra-mongo-1.0.0-DEV`
 - Station Captain - `manager-station+captain-1.0.0-DEV`

### Installers

All installer/ package names need to follow the following format:

`open+quarter+master-{type}[-{source}]-{packageName}`

Installer name convention is to use the package name, plus the version and installer extension. All parts of the package name must be lower-case, and use `+` instead of spaces, with no other punctuation.

Examples:

- Station Captain: `open+quarter+master-infra-station+captain`
- Station Captain installer (deb): `open+quarter+master-infra-station+captain-1.0.0-DEV.deb`
- External plugin: `open+quarter+master-plugin-other+org+repo-plugin+name`
