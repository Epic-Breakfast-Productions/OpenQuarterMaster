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

def listAll(args):
    print(json.dumps(mainCM.configData, indent=4))

def get(args):
    configToGet = args.key
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

def template(args):
    configFileToGet = args.templateFile
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

def set(args):
    configKeyToSet = args.key
    configValToSet = args.value
    configFile = args.file
    secret = args.secret

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

# Setup argument parser
argParser = argparse.ArgumentParser(
    prog="oqm-config",
    description="This script is a utility to help manage openQuarterMaster's configuration."
)

g = argParser.add_mutually_exclusive_group()
g.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")

subparsers = argParser.add_subparsers(dest="command", help="Subcommands")

list_parser = subparsers.add_parser("list", aliases=['l'], help="Lists current config.")
list_parser.set_defaults(func=listAll)

get_parser = subparsers.add_parser("get", aliases=['g'], help="Gets a config's value.")
get_parser.add_argument("key", help="The config key to get.")
get_parser.set_defaults(func=get)

get_parser = subparsers.add_parser("template", aliases=['t'], help="Fills out a template file with config values. Outputs the result.")
get_parser.add_argument("templateFile", help="The template file to fill out.")
get_parser.set_defaults(func=template)

set_parser = subparsers.add_parser("set", aliases=['s'], help="Sets a configuration value.")
set_parser.add_argument("key", help="The config key to set.")
set_parser.add_argument("value", help="The value to set.")
set_parser.add_argument("file", help="The file to modify.")
set_parser.add_argument('--secret', '-s', action='store_true', help='Specifies this config value is to be stored as a secret.')
set_parser.set_defaults(func=set)

args = argParser.parse_args()

if args.v:
    printVersion()
    exit(0)

if hasattr(args, 'func'):
    args.func(args)
else:
    print("No input given.")
    argParser.print_help()
