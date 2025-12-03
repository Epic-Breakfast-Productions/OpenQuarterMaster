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
from DemoModeUtils import *
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
# g.add_argument('-vvvv', '--verbose', dest="verbose", action="store_false", help="Tells the script to log output verbosely to the console") # TODO:: fix

subparsers = argParser.add_subparsers(dest="command", help="Subcommands")

SnapshotUtils.setupArgParser(subparsers)
ContainerUtils.setupArgParser(subparsers)
LogManagement.setupArgParser(subparsers)
CertsUtils.setupArgParser(subparsers)
DemoModeUtils.setupArgParser(subparsers)

# TODO:: registration and subscription utility
# TODO:: plugin utilities

# TODO:: command to handle all init service setup; container, certs...

argcomplete.autocomplete(argParser)
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

log.info("==== END OF OQM-CAPTAIN SCRIPT ====")
