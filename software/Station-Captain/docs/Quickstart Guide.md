# Station Captain Quickstart

[Back](README.md)

## Introduction

This software installs and manages an instance of Open QuarterMaster. It is designed to make the process easier and streamlined for everyday users, running on their own hardware.

Note: this is not a tool meant for a distributed cloud environment such as Kubernetes. This sets up the Open QuarterMaster system on a single computer/node.

## Installation

Steps:

1. Run the following command: `bash <(curl -s https://deployment.openquartermaster.com/deb-ppa/setup-repo.sh)`
    - This will setup the OQM repo, and install `oqm-captain` for you.
4. Run the main script command: `sudo oqm-captain`
    - The first run should prompt you to do an initial install. Do so.
    - Installation should be complete once this finishes. You can exit the script.
5. You can navigate to your computer's ip or domain from a web browser to access the Open QuarterMaster tool.
    - Tip: the `oqm-captain` tool lists your ip under `Info / Status`/`Host / Base OS`

For usage documentation, see the [User Guide](User%20Guide.adoc)

## Requirements

This is a set of requirements for the entire system, not necessarily just the station captain.

This system is regularly tested on Ubuntu 22.04.

### System Requirements:

- A Modern Linux OS
  - At the moment we only support Debian-based systems (`apt`), we plan on eventually also supporting Fedora/RHEL (`dnf`) systems. (Tested on Ubuntu 22.04, Raspbian)
- 4(quad) cores
- 8gb of RAM
- CPU Requirements
    - Quad-core (or 2 code, with two threads each)
    - One of the following architectures:
      - 64-bit `x64` ISA, intel or AMD
        - Must include [AVX](https://en.wikipedia.org/wiki/Advanced_Vector_Extensions). Known supported:
          - Any AMD
          - Any modern Intel i3, i5, i7, i9
          - Intel N95, N100
      - 64-bit ARM v8

Note:: it is possible certain plugins, or a large number of plugins, will require more resources or have different hardware requirements.

### Proven SBC's and other 'specialty' hardware:

 - ARM:
   - [Raspberry Pi 5B](https://www.raspberrypi.com/products/raspberry-pi-5/), 8Gb memory recommended. 4Gb would probably be fine for testing purposes only, but any less is probably not going to be enough for a good experience.

