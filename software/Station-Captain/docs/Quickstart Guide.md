# Station Captain Quickstart

## Introduction

This software installs and manages an instance of Open QuarterMaster. It is designed to make the process easier and streamlined for everyday users, running on their own hardware.

Note: this is not a tool meant for a distributed cloud environment such as Kubernetes. This sets up the Open QuarterMaster system 

## Requirements

This is a set of requirements for the entire system, not necessarily just the station captain.

### System Requirements:

- A Modern Linux OS
  - At the moment we only support Debian-based systems (`apt`), we plan on eventually also supporting Fedora/RHEL (`dnf`) systems. (Tested on Ubuntu 22.04)
- 4(quad) cores
- 8gb of RAM
- Any amdx64-bit architecture (any modern Intel or AMD cpu), or Arm v8.

### Proven SBC's and other 'specialty' hardware:

 - ARM:
   - [Raspberry Pi 5B](https://www.raspberrypi.com/products/raspberry-pi-5/) (to be proven, but should work)

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
