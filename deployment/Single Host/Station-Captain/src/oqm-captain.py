#!/bin/python3
# PYTHON_ARGCOMPLETE_OK
import os
import sys
import logging
sys.path.append("lib/")
from LogUtils import *
LogUtils.setupLogging("station-captain.log", "--verbose" in sys.argv)

from ScriptInfos import ScriptInfo
import UserInteraction
from SnapshotUtils import *
from ContainerUtils import *
from LogManagement import *
from CertsUtils import *
import argparse
import argcomplete
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

log = LogUtils.setupLogger(__name__)

log.info("==== STARTING OQM-CAPTAIN SCRIPT ====")

argParser = argparse.ArgumentParser(
    prog="oqm-captain",
    description="This script is a utility to help manage an installation of Open QuarterMaster. Must be run as root.",
    epilog="Script version "+ScriptInfo.SCRIPT_VERSION+". With <3, EBP"
)
g = argParser.add_mutually_exclusive_group()
g.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
# g.add_argument('-vvvv', '--verbose', dest="verbose", action="store_false", help="Tells the script to log output verbosely to the console") # TODO:: fix
g.add_argument('--take-snapshot', dest="takeSnapshot", help="Takes a snapshot. Will pause and restart services.", choices=["manual", "scheduled", "preemptive"])
g.add_argument('--prune-container-resources', dest="pruneContainerResources", action="store_true", help="Prunes all unused container resources. Roughly equivalent to running both `docker system prune --volumes` and `docker image prune -a`")
g.add_argument('--ensure-container-setup', dest="ensureContainerSetup", action="store_true", help="Ensures all container based resources (i.e, network) are setup and ready.")
g.add_argument('--package-logs', dest="packageLogs", action="store_true", help="Packages service logs for debugging.")
g.add_argument('--regen-certs', dest="regenCerts", action="store_true", help="Regenerates the system certs based on configuration.")
g.add_argument('--ensure-certs-present', dest="ensureCerts", action="store_true", help="Ensures that certs are present and usable by the system.")
# argcomplete.autocomplete(argParser)
args = argParser.parse_args()

# print(str(args))
if args.v:
    print(ScriptInfo.SCRIPT_VERSION)
    exit(0)

if not os.geteuid() == 0:
    print("\n\nPlease run this script as root. ( sudo oqm-captain )\n")
    exit(1)

if args.takeSnapshot:
    trigger = SnapshotTrigger[args.takeSnapshot]
    success, message = SnapshotUtils.performSnapshot(trigger)
    if not success:
        print("FAILED to create snapshot: " + message, file=sys.stderr)
        exit(2)
elif args.pruneContainerResources:
    ContainerUtils.pruneContainerResources()
elif args.ensureContainerSetup:
    ContainerUtils.ensureSharedDockerResources()
elif args.packageLogs:
    result, message = LogManagement.packageLogs()
    if not result:
        print("Failed to package logs: " + message)
        exit(3)
    print(message)
elif args.regenCerts:
    result, message = CertsUtils.regenCerts()
    if not result:
        print("Failed to generate certs: " + message)
        exit(4)
    print(message)
elif args.ensureCerts:
    result, message = CertsUtils.ensureCertsPresent()
    if not result:
        print("Failed to validate certs: " + message)
        exit(5)
    print(message)
else:
    UserInteraction.ui.startUserInteraction()

log.info("==== END OF OQM-CAPTAIN SCRIPT ====")
