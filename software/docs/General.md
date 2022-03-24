# General

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
