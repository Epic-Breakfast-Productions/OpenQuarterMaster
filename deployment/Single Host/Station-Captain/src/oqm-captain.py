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
import atexit
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

log = LogUtils.setupLogger("main")
log.info("==== STARTING OQM-CAPTAIN SCRIPT ====")

def handleExit():
    log.info("==== END OF OQM-CAPTAIN SCRIPT ====")
atexit.register(handleExit)

argParser = argparse.ArgumentParser(
    prog="oqm-captain",
    description="This script is a utility to help manage an installation of Open QuarterMaster. Must be run as root.",
    epilog="Script version "+ScriptInfo.SCRIPT_VERSION+". With <3, EBP"
)
g = argParser.add_mutually_exclusive_group()
g.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")

subparsers = argParser.add_subparsers(dest="command", help="Subcommands")

# TODO:: in following, update function references

snapshot_parser = subparsers.add_parser("take-snapshot", help="Triggers a snapshot.")
snapshot_parser.add_argument(dest="mode", help="What is triggering the snapshot. Defaults to 'manual'.", choices=["manual", "scheduled", "preemptive"], default="manual", nargs='?')
snapshot_parser.set_defaults(func=lambda funcArgs: print("In the thing: ", funcArgs))

prune_parser = subparsers.add_parser("prune-container-resources", help="Prunes all unused container resources. Roughly equivalent to running both `docker system prune --volumes` and `docker image prune -a`")
prune_parser.set_defaults(func=lambda funcArgs: print("In the thing: ", funcArgs))

ecs_parser = subparsers.add_parser("ensure-container-setup", help="Ensures all container based resources (i.e, network) are setup and ready.")
ecs_parser.set_defaults(func=lambda funcArgs: print("In the thing: ", funcArgs))

logs_parser = subparsers.add_parser("package-logs", help="Packages service logs for debugging.")
logs_parser.set_defaults(func=lambda funcArgs: print("In the thing: ", funcArgs))

rc_parser = subparsers.add_parser("regen-certs", help="Regenerates the system certs based on configuration.")
rc_parser.set_defaults(func=lambda funcArgs: print("In the thing: ", funcArgs))

ecp_parser = subparsers.add_parser("ensure-certs-present", help="Ensures that certs are present and usable by the system.")
ecp_parser.set_defaults(func=lambda funcArgs: print("In the thing: ", funcArgs))

ecp_parser = subparsers.add_parser("write-internal-certs", help="Writes certs for an internal service to use.")
ecp_parser.add_argument(dest="service", help="The name of the service (the domain name to access the service).")
ecp_parser.add_argument(dest="destination", help="The directory to place the new certs.")
ecp_parser.set_defaults(func=lambda funcArgs: print("In the thing: ", funcArgs))


# g.add_argument('-vvvv', '--verbose', dest="verbose", action="store_false", help="Tells the script to log output verbosely to the console") # TODO:: fix

# argcomplete.autocomplete(argParser)

args = argParser.parse_args()

# print(str(args))
if args.v:
    print(ScriptInfo.SCRIPT_VERSION)
    exit(0)

if not os.geteuid() == 0:
    print("\n\nPlease run this script as root. ( sudo oqm-captain )\n")
    exit(1)

if hasattr(args, 'func'):
    args.func(args)
else:
    UserInteraction.ui.startUserInteraction()

#
# if args.takeSnapshot:
#     trigger = SnapshotTrigger[args.takeSnapshot]
#     success, message = SnapshotUtils.performSnapshot(trigger)
#     if not success:
#         print("FAILED to create snapshot: " + message, file=sys.stderr)
#         exit(2)
# elif args.pruneContainerResources:
#     ContainerUtils.pruneContainerResources()
# elif args.ensureContainerSetup:
#     ContainerUtils.ensureSharedDockerResources()
# elif args.packageLogs:
#     result, message = LogManagement.packageLogs()
#     if not result:
#         print("Failed to package logs: " + message)
#         exit(3)
#     print(message)
# elif args.regenCerts:
#     result, message = CertsUtils.regenCerts()
#     if not result:
#         print("Failed to generate certs: " + message)
#         exit(4)
#     print(message)
# elif args.ensureCerts:
#     result, message, written = CertsUtils.ensureCoreCerts()
#     if not result:
#         print("Failed to validate certs: " + message)
#         exit(5)
#     print(message)
# elif args.writeInternalCerts:
#     result, message = CertsUtils.generateInternalCert(
#         args.writeInternalCerts[0],
#         args.writeInternalCerts[1]
#     )
#     if not result:
#         print("Failed to write certs for internal service: " + message)
#         exit(6)
#     print(message)
# else:
#     UserInteraction.ui.startUserInteraction()

log.info("==== END OF OQM-CAPTAIN SCRIPT ====")
