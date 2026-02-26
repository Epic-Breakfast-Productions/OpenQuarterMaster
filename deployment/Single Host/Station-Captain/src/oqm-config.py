#!/usr/bin/python3
# PYTHON_ARGCOMPLETE_OK
#
# Script to get configuration and replace values
#
import os
import sys
import time
from encodings.aliases import aliases

from jinja2 import FileSystemLoader

sys.path.append("lib/")
from LogUtils import *
LogUtils.setupLogging("config.log")

from ConfigManager import *
from ScriptInfos import *
import json
import argparse
import argcomplete
import re
import jinja2
import atexit
import pathlib
from os import listdir
from os.path import isfile, join

SCRIPT_TITLE = "Open QuarterMaster Station Config Helper v" + ScriptInfo.SCRIPT_VERSION

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

def listKeysRec(data, list, prevKey = ""):
    for key in data:
        fullKey = prevKey + "." + key if prevKey else key
        list.append(fullKey)
        if isinstance(data[key], dict):
            listKeysRec(data[key], list, fullKey)

def listKeys(args, printOut=True):
    prefix=args.prefix

    keyList=[]
    listKeysRec(mainCM.configData, keyList)

    keyList = [s for s in keyList if s.startswith(prefix)]

    if printOut:
        print("\n".join(keyList))
    return keyList

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


def wait(args):
    returnVal = args.returnVal
    result, configValue = mainCM.waitForConfig(args.key, args.timeout)
    if not result:
        print("ERROR: Timeout waiting for config value after "+str(args.timeout)+"s.", file=sys.stderr)
        exit(1)
    if returnVal:
        if isinstance(configValue, (dict, list)):
            configValue = json.dumps(
                configValue,
                indent=4
            )
        print(configValue)


def template(args):
    configFileToGet = args.templateFile
    try:
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
            if not curPlaceholder.strip():
                continue
            # print("debug: resolving placeholder: " + curPlaceholder)
            output = output.replace(
                "{" + curPlaceholder + "}",
                mainCM.getConfigVal(curPlaceholder)
            )
    except Exception as e:
        print("ERROR: Failed to template file ("+str(configFileToGet)+"): " + str(e), file=sys.stderr)
        exit(1)

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

# https://kislyuk.github.io/argcomplete/#specifying-completers
class ConfigKeyCompleter(object):
    def __call__(self, prefix, **kwargs):
        obj = lambda: None
        obj.prefix = prefix
        return listKeys(obj, False)

class ConfigFileCompleter(object):
    def __call__(self, prefix, **kwargs):
        # list files in config dir
        output = [f for f in listdir(ScriptInfo.CONFIG_VALUES_DIR) if isfile(join(ScriptInfo.CONFIG_VALUES_DIR, f)) and f.endswith(".json") and f.startswith(prefix)]

        if CONFIG_MNGR_DEFAULT_ADDENDUM_FILENAME not in output:
            output.append(CONFIG_MNGR_DEFAULT_ADDENDUM_FILENAME)
        return output

# Setup argument parser
argParser = argparse.ArgumentParser(
    prog="oqm-config",
    description="This script is a utility to help manage Open QuarterMaster's configuration on this host."
)

g = argParser.add_mutually_exclusive_group()
g.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")

subparsers = argParser.add_subparsers(dest="command", help="Subcommands")

list_parser = subparsers.add_parser("list", aliases=['l'], help="Lists current config with values. Does not fill out secret values.")
list_parser.set_defaults(func=listAll)

key_list_parser = subparsers.add_parser("list-keys", aliases=['lk'], help="Lists all config keys.")
key_list_parser.add_argument("prefix", help="The config key prefix to specify (optional).", nargs="?", default="").completer=ConfigKeyCompleter()
key_list_parser.set_defaults(func=listKeys)

get_parser = subparsers.add_parser("get", aliases=['g'], help="Gets a config's value.")
get_parser.add_argument("key", help="The config key to get.").completer=ConfigKeyCompleter()
get_parser.set_defaults(func=get)

wait_parser = subparsers.add_parser("wait", aliases=['w'], help="Waits for a config value to be populated before returning.")
wait_parser.add_argument("key", help="The config key to check.").completer=ConfigKeyCompleter()
wait_parser.add_argument("timeout", help="How long to wait before timing out, in seconds (optional).", type=int, nargs="?", default=30)
wait_parser.add_argument("returnVal", help="If to return (print) the value after waiting (optional).", type=bool, nargs="?", default=False)
wait_parser.set_defaults(func=wait)

temp_parser = subparsers.add_parser("template", aliases=['t'], help="Fills out a template file with config values. Outputs the result.")
temp_parser.add_argument("templateFile", help="The template file to fill out.", type=pathlib.Path)
temp_parser.set_defaults(func=template)

set_parser = subparsers.add_parser("set", aliases=['s'], help="Sets a configuration value.")
set_parser.add_argument("key", help="The config key to set.").completer=ConfigKeyCompleter()
set_parser.add_argument("value", help="The value to set.")
set_parser.add_argument("file", help="The file in the config directory to modify. Optional. Omit or specify empty to default to \"99-custom.json\".", nargs="?", default="").completer=ConfigFileCompleter()
set_parser.add_argument('--secret', '-s', action='store_true', help='Specifies this config value is to be stored as a secret.')
set_parser.set_defaults(func=set)

argcomplete.autocomplete(argParser)
args = argParser.parse_args()

if args.v:
    printVersion()
    exit(0)

if hasattr(args, 'func'):
    args.func(args)
else:
    print("No input given.")
    argParser.print_help()
