#!/usr/bin/python3
#
# Script to get configuration and replace values
#
import json
import os
import argparse
import re
import sys

SCRIPT_VERSION = 'SCRIPT_VERSION'
SCRIPT_TITLE = "Open QuarterMaster Station Config Helper V${SCRIPT_VERSION}"
CONFIGS_DIR = "/etc/oqm/config"
MAIN_CONFIG_FILE = CONFIGS_DIR + "/mainConfig.yaml"
ADD_CONFIG_DIR = CONFIGS_DIR + "/configs"

EXIT_CANT_READ_FILE = 2
EXIT_BAD_CONFIG_KEY = 3

configData = {}

# Ensure main config, additional configs dir exist
os.makedirs(ADD_CONFIG_DIR, exist_ok=True)
if not os.path.isfile(MAIN_CONFIG_FILE):
    with open(MAIN_CONFIG_FILE, 'x') as stream:
        stream.write('''
{
    "captain": {
        "test": "hello"
    }
}
''')


class ConfigKeyNotFoundException(Exception):
    pass


def readFile(file: str) -> dict:
    """
    Reads a file into a python dict
    :param file:
    :return:
    """
    with open(file, 'r') as stream:
        return json.load(stream)


configData = readFile(MAIN_CONFIG_FILE)
for file in os.listdir(ADD_CONFIG_DIR):
    if file.endswith(".json"):
        curUpdates = readFile(ADD_CONFIG_DIR + "/" + file)
        configData.update(curUpdates)
    else:
        continue


def getConfigValRec(configKey: str, data: dict = configData, format=True) -> str:
    # print("debug:: configKey: " + configKey)
    # print("debug:: data: " + json.dumps(data, indent=4))
    if "." in configKey:
        parts = configKey.split(".", 1)
        curConfig = parts[0]
        keyLeft = parts[1]

        if curConfig not in data:
            raise ConfigKeyNotFoundException()

        return getConfigValRec(keyLeft, data[curConfig])
    if configKey not in data:
        raise ConfigKeyNotFoundException()
    result = data[configKey]
    if isinstance(result, (dict, list)):
        if format:
            result = json.dumps(result, indent=4)
        else:
            result = json.dumps(result)
    elif not isinstance(result, str):
        result = str(result)
    return result


def getConfigVal(configKey: str, data: dict = configData, format=True) -> str:
    try:
        return getConfigValRec(configKey, data, format)
    except ConfigKeyNotFoundException:
        print("ERROR: Config key not found: " + configKey, file=sys.stderr)
        exit(EXIT_BAD_CONFIG_KEY)


argParser = argparse.ArgumentParser(
    prog=SCRIPT_TITLE,
    description="This script is a utility to help manage openQuarterMaster's configuration."
)
argParser.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
argParser.add_argument('-l', '--list', dest="l", action="store_true", help="List all available configuration vales")
argParser.add_argument('-g', '--get', dest="g", help="Gets a config's value.", nargs=1)
argParser.add_argument('-t', '--template', dest="t",
                       help="Supply a file to replace placeholders in. Outputs the result.", nargs=1)

args = argParser.parse_args()

try:
    if args.v:
        print(SCRIPT_TITLE)
    elif args.l:
        print(json.dumps(configData, indent=4))
    elif args.g:
        configToGet = args.g[0]
        configValue = getConfigVal(configToGet)
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
            output = output.replace("{" + curPlaceholder + "}", getConfigVal(curPlaceholder, format=False))

        print(output)
    else:
        print("No input given.")
        argParser.print_help()
except RuntimeError:
    print()
