# Station Captain Quickstart

[Back](README.md)

## Introduction

This software installs and manages an instance of Open QuarterMaster. It is designed to make the process easier and streamlined for everyday users, running on their own hardware.

Note: this is not a tool meant for a distributed cloud environment such as Kubernetes. This sets up the Open QuarterMaster system on a single computer/node.

[System Requirements](System%20Requirements.md)

Having issues? Check out the [Gotchas, FAQ, & Troubleshooting](tgf.md) page. The "gotchas" section would be a great readthrough to ensure you get up an running quickly!

## Installation

Steps:

1. Run the following command: `bash <(wget -q -O - https://deployment.openquartermaster.com/repos/main/deb/setup-repo.sh)`
    - This will setup the OQM repo, and install `oqm-captain` for you.
    - Might require installing `wget`: `sudo apt install wget`
4. Run the main script command: `sudo oqm-captain`
    - The first run should prompt you to go through a setup wizard to get you going. It is recommended to do go through it.
    - Installation should be complete once this finishes. You can exit the script.
5. You can navigate to your computer's ip or domain from a web browser to access the Open QuarterMaster system.
    - Tip: the `oqm-captain` tool lists your ip under `Info / Status`/`Host / Base OS`
    - Tip: if installed on a system with a GUI (Ubuntu desktop for instance), you will have app shortcuts available to you now. Look for "OQM"!
