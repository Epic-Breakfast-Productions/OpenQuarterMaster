#!/usr/bin/python3
#
# Script to get configuration and replace values
#
import os
import sys

from jinja2 import FileSystemLoader

sys.path.append("lib/")
from LogUtils import *
LogUtils.setupLogging("config.log")

from ConfigManager import *
from ScriptInfos import *
import json
import argparse
import re
import jinja2
import atexit

SCRIPT_TITLE = "Open QuarterMaster Station Config Helper V" + ScriptInfo.SCRIPT_VERSION

EXIT_CANT_READ_FILE = 2
EXIT_BAD_CONFIG_KEY = 3
EXIT_CONFIG_READ_ERR = 4
EXIT_CONFIG_INIT_ERR = 5
EXIT_CONFIG_SET_ERR = 6

log = LogUtils.setupLogger(__name__)

log.info("==== STARTING OQM-CONFIG SCRIPT ====")
def handleExit():
    log.info("==== END OF OQM-CONFIG SCRIPT ====")
atexit.register(handleExit)

def printVersion():
    print(SCRIPT_TITLE)

def listAll():
    print(json.dumps(mainCM.configData, indent=4))

def get(configToGet):
    try:
        configValue = mainCM.getConfigVal(configToGet)
        if isinstance(configValue, (dict, list)):
            configValue = json.dumps(
                configValue,
                indent=4
            )
    except ConfigKeyNotFoundException:
        print("ERROR: Config key not found: " + configToGet, file=sys.stderr)
        exit(1)
    print(configValue)

def template(configFileToGet):
    configFileToGetPath, configFileToGetFilename = os.path.split(configFileToGet)

    environment = jinja2.Environment(loader=FileSystemLoader(configFileToGetPath))
    # template = environment.from_string(output)
    template = environment.get_template(configFileToGetFilename)
    output = template.render(mainCM.getFilledOutData())


    # output = ""
    # try:
    #     with open(configFileToGet, 'r') as file:
    #         output = file.read()
    # except OSError as e:
    #     print("Failed to read file: ", e, file=sys.stderr)
    #     exit(EXIT_CANT_READ_FILE)

    placeholders = re.findall(r'\{(.*?)}', output)
    for curPlaceholder in placeholders:
        # print("debug: resolving placeholder: " + curPlaceholder)
        output = output.replace(
            "{" + curPlaceholder + "}",
            mainCM.getConfigVal(curPlaceholder)
        )

    print(output)

def set(configKeyToSet, configValToSet, configFile, secret=False):
    if secret:
        json = mainCM.setSecretValInFile(
            configKeyToSet=args.setSecret[0],
            configValToSet=args.setSecret[1],
            configFile=args.setSecret[2]
        )
    else:
        json = mainCM.setConfigValInFile(
            configKeyToSet=configKeyToSet,
            configValToSet=configValToSet,
            configFile=configFile
        )
    # TODO: error check
    print(json)

def setSecret():


# Setup argument parser
argParser = argparse.ArgumentParser(
    prog="oqm-config",
    description="This script is a utility to help manage openQuarterMaster's configuration."
)

g = argParser.add_mutually_exclusive_group()
g.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")



g.add_argument('-l', '--list', dest="l", action="store_true", help="List all available configuration vales")
g.add_argument('-g', '--get', dest="g", help="Gets a config's value.", nargs=1)
g.add_argument('-t', '--template', dest="t",
                       help="Supply a file to replace placeholders in. Outputs the result.", nargs=1)
g.add_argument('-s', '--set', dest="s",
                       help="Sets a value. First arg is the key, second is the value to set, third is the file to modify (The file in the " + ScriptInfo.CONFIG_VALUES_DIR + " directory)(empty string for default additional file (" + CONFIG_MNGR_DEFAULT_ADDENDUM_FILE + ")).",
                       nargs=3)
g.add_argument('-S', '--setSecret', dest="setSecret",
                       help="Sets a secret value. First arg is the key, second is the value to set, third is the file to modify (The file in the " + ScriptInfo.CONFIG_VALUES_DIR + " directory)(empty string for default additional file (" + CONFIG_MNGR_DEFAULT_ADDENDUM_FILE + ")).",
                       nargs=3)

args = argParser.parse_args()

if args.v:
    printVersion()
elif args.l:
    listAll()
elif args.g:
    get(args.g[0])
elif args.t:
    template(args.t[0])
elif args.s:
    set(args.s[0], args.s[1], args.s[2])
elif args.setSecret:
    set(args.s[0], args.s[1], args.s[2], True)
else:
    print("No input given.")
    argParser.print_help()
