#!/bin/python3
import os
import sys
import logging
sys.path.append("lib/")
from ScriptInfos import ScriptInfo
import UserInteraction
from SnapshotUtils import *
from ContainerUtils import *
from LogManagement import *
import argparse
# This script manages an installation of Open QuarterMaster on a single host
#
# Author: Greg Stewart
# https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster
#
# requires:
#  - dialog
#  - docker
#  - hwinfo
#  - sponge (from moreutils)
#  - python3-dialog

# https://click.palletsprojects.com/en/8.1.x/
# https://pythondialog.sourceforge.io/

logging.basicConfig(level=logging.DEBUG)
argParser = argparse.ArgumentParser(
    prog="oqm-captain",
    description="This script is a utility to help manage an installation of Open QuarterMaster. Must be run as root.",
    epilog="Script version "+ScriptInfo.SCRIPT_VERSION+". With <3, EBP"
)
argParser.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
argParser.add_argument('--take-snapshot', dest="takeSnapshot", action="store_true", help="Takes a snapshot. Will pause and restart services.")
argParser.add_argument('--prune-container-resources', dest="pruneContainerResources", action="store_true", help="Prunes all unused container resources. Roughly equivalent to running both `docker system prune --volumes` and `docker image prune -a`")
argParser.add_argument('--ensure-container-setup', dest="ensureContainerSetup", action="store_true", help="Ensures all container based resources (i.e, network) are setup and ready.")
argParser.add_argument('--package-logs', dest="packageLogs", action="store_true", help="Packages service logs for debugging.")
args = argParser.parse_args()

if args.v:
    print(ScriptInfo.SCRIPT_VERSION)
    exit(1)

if not os.geteuid() == 0:
    print("\n\nPlease run this script as root. ( sudo oqm-captain )\n")
    exit(2)

if args.takeSnapshot:
    trigger = SnapshotTrigger.manual
    if args.takeSnapshot[0]:
        trigger = SnapshotTrigger(args.takeSnapshot[0])
    SnapshotUtils.performSnapshot(trigger)
if args.pruneContainerResources:
    ContainerUtils.pruneContainerResources()
if args.ensureContainerSetup:
    ContainerUtils.ensureSharedDockerResources()
if args.packageLogs:
    result, message = LogManagement.packageLogs()
    if not result:
        print("Failed to package logs: " + message)
        exit(2)
    print(message)
else:
    UserInteraction.ui.startUserInteraction()
