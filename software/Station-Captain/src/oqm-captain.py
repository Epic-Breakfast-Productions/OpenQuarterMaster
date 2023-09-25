#!/bin/python3

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
import sys
import logging
sys.path.append("lib/")
from ScriptInfos import ScriptInfo
import UserInteraction

logging.basicConfig(level=logging.DEBUG)

UserInteraction.ui.mainMenu()
