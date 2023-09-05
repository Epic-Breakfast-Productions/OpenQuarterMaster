#!/usr/bin/python3
#
# Script to get configuration and replace values
#
from lib.ConfigManager import *
import json
import argparse
import re
import sys


SCRIPT_VERSION = 'SCRIPT_VERSION'
SCRIPT_TITLE = "Open QuarterMaster Station Config Helper V" + SCRIPT_VERSION

EXIT_CANT_READ_FILE = 2
EXIT_BAD_CONFIG_KEY = 3
EXIT_CONFIG_READ_ERR = 4
EXIT_CONFIG_INIT_ERR = 5
EXIT_CONFIG_SET_ERR = 6

# Setup argument parser
argParser = argparse.ArgumentParser(
    prog="oqm-config",
    description="This script is a utility to help manage openQuarterMaster's configuration."
)
argParser.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
argParser.add_argument('-l', '--list', dest="l", action="store_true", help="List all available configuration vales")
argParser.add_argument('-g', '--get', dest="g", help="Gets a config's value.", nargs=1)
argParser.add_argument('-t', '--template', dest="t",
                       help="Supply a file to replace placeholders in. Outputs the result.", nargs=1)
argParser.add_argument('-s', '--set', dest="s",
                       help="Sets a value. First arg is the key, second is the value to set, third is the file to modify (The file in the " + CONFIG_MNGR_ADD_CONFIG_DIR + " directory)(empty string for default additional file (" + CONFIG_MNGR_DEFAULT_ADDENDUM_FILE + ")).",
                       nargs=3)

configManager = ConfigManager()

args = argParser.parse_args()

if args.v:
    print(SCRIPT_TITLE)
elif args.l:
    print(json.dumps(configManager.configData, indent=4))
elif args.g:
    configToGet = args.g[0]
    configValue = configManager.getConfigVal(configToGet, configManager.configData)
    print(configValue)
elif args.t:
    configFileToGet = args.t[0]
    output = ""
    try:
        with open(configFileToGet, 'r') as file:
            output = file.read()
    except OSError as e:
        print("Failed to read file: ", e, file=sys.stderr)
        exit(EXIT_CANT_READ_FILE)
    placeholders = re.findall(r'\{(.*?)}', output)

    for curPlaceholder in placeholders:
        # print("debug: resolving placeholder: " + curPlaceholder)
        output = output.replace(
            "{" + curPlaceholder + "}",
            configManager.getConfigVal(curPlaceholder, configManager.configData, formatData=False)
        )

    print(output)
elif args.s:
    json = ConfigManager.setConfigValInFile(
        configKeyToSet=args.s[0],
        configValToSet=args.s[1],
        configFile=args.s[2]
    )
    # TODO: error check
    print(json)

else:
    print("No input given.")
    argParser.print_help()
