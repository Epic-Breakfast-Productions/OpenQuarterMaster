# Station Captain Quickstart

## Introduction

This software installs and manages an instance of Open QuarterMaster. It is designed to make the process easier and streamlined for everyday users, running on their own hardware.

Note: this is not a tool meant for a cloud environment such as Kubernetes. All software (Base Station, plugins) are designed on their own to be deployable on a Docker environment, so if you are running such an environment, this is not the tool for you!

## Requirements

This is a set of requirements for the entire system, not necessarily just the station captain.

System Requirements:

- A Modern Linux OS
  - At the moment we only support Debian-based systems (`apt`), we plan on eventually also supporting Fedora/RHEL (`dnf`) systems.
- 4gb of RAM
- Any amdx64-bit architecture (any modern Intel or AMD cpu), or Arm v8
  - Proven arm boards:
    - Raspberry Pi 4B

## Installation

Steps:

 1. Download the installer for your system [here on the releases page](https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/releases?q=Station+captain&expanded=true).
    - Curl command to download: `curl -ks https://api.github.com/repos/Epic-Breakfast-Productions/OpenQuarterMaster/releases/latest | grep "browser_download_url.*.deb" | cut -d : -f 2,3 | tr -d \" | xargs wget` 
 3. Install the package using:
    - `sudo apt install ./<deb file>.deb`
 4. Run the main script command: `sudo oqm-captain`
    - The first run should prompt you to do an initial install. Do so.
    - Installation should be complete once this finishes. You can exit the script.
 5. You can navigate to your computer's ip or domain from a web browser to access the Open QuarterMaster tool.
    - Tip: the `oqm-captain` tool lists your ip under `Info / Status`/`Host / Base OS`

For usage documentation, see the [User Guide](User%20Guide.adoc)
