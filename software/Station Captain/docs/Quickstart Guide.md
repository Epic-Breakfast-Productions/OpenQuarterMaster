# Station Captain Quickstart

## Introduction

This software installs and manages an instance of Open QuarterMaster. It is designed to make the process easier and streamlined for everyday users, running on their own hardware.

Note: this is not a tool meant for a cloud environment such as Kubernetes. All software (Base Station, plugins) are designed on their own to be deployable on a Docker environment, so if you are running such an environment, this is not the tool for you!

## Requirements

This is a set of requirements for the entire system, not necessarily just the station captain.

System Requirements:

- A Modern Linux OS
  - At the moment we only support Debian-based systems (`apt`), we plan on eventually also supporting Fedora/RHEL (`yum`) systems.
- 4gb of RAM

## Installation

Steps:

1. Download the installer for your system [here on the releases page](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/releases?q=Station+captain&expanded=true).
2. Install the package using:
  - `sudo apt install <deb file>.deb`
3. Run the main script command: `sudo oqm-captain`

For usage documentation, see the [User Guide](User%20Guide.md)
