#!/usr/bin/python3
#
# Script to get configuration and replace values
#
from lib import ConfigManager
import json
from json import JSONDecodeError
import os
import argparse
import re
import sys

SCRIPT_VERSION = 'SCRIPT_VERSION'
SCRIPT_TITLE = "Open QuarterMaster Station Config Helper V" + SCRIPT_VERSION
CONFIGS_DIR = "/etc/oqm/config"
MAIN_CONFIG_FILE = CONFIGS_DIR + "/mainConfig.json"
ADD_CONFIG_DIR = CONFIGS_DIR + "/configs"
DEFAULT_ADDENDUM_FILE = "99-custom.json"

KEYRING = "/etc/oqm/keyring.SimpleKeyring"
SECRET_PLACEHOLDER = "<secret>"

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
                       help="Sets a value. First arg is the key, second id the value to set, third is the file to modify (The file in the " + ADD_CONFIG_DIR + " directory)(empty string for default additional file (" + DEFAULT_ADDENDUM_FILE + ")).",
                       nargs=3)

# Will contain all data read in from files.
configData = {}

# Ensure main config, additional configs dir exist
try:
    os.makedirs(ADD_CONFIG_DIR, exist_ok=True)
    if not os.path.isfile(MAIN_CONFIG_FILE):
        with open(MAIN_CONFIG_FILE, 'x') as stream:
            stream.write('''
{
    "captain": {
    },
    "snapshots": {
        "location": "/data/oqm-snapshots/",
        "numToKeep": 5,
        "frequency": "weekly"
    }
}
''')
except OSError as e:
    print("Error: failed to setup default configuration. Try running as root, at least for first run. Error: ", e,
          file=sys.stderr)
    exit(EXIT_CONFIG_INIT_ERR)


def readFile(file: str) -> dict:
    """
    Reads a file into a python dict. If error in reading or parsing, exits
    :param file:
    :return:
    """
    try:
        with open(file, 'r') as stream:
            return json.load(stream)
    except (OSError, JSONDecodeError) as e:
        print("Error: failed to read file " + file + " into configuration: ", e, file=sys.stderr)
        exit(EXIT_CONFIG_READ_ERR)


# Read in configuration
configData = readFile(MAIN_CONFIG_FILE)
for file in os.listdir(ADD_CONFIG_DIR):
    if file.endswith(".json"):
        curUpdates = readFile(ADD_CONFIG_DIR + "/" + file)
        configData.update(curUpdates)
    else:
        continue


# Exception to throw when config errors occur
class ConfigKeyNotFoundException(Exception):
    pass


def isSecret(configVal: str):
    return SECRET_PLACEHOLDER == configVal


def getSecret(configKey: key):
    """
    https://github.com/jaraco/keyring#using-keyring-on-headless-linux-systems-in-a-docker-container

    echo "hello world" | keyring -p "./test.kr" set "testService" "me"
    keyring -p "test.kr" get "testService" "me"
    :param configKey:
    :return:
    """
    keyring.set_keyring(KEYRING)
    return ""


def getConfigValRec(configKey: str, data: dict, format) -> str:
    """
    Recursive function to get a particular value in the data
    :param configKey: The configuration to get, dot notation I.E. "test.value"
    :param data: The dict to find the configuration in
    :param format: If returning an object or list, if to format the data 'pretty'
    :except ConfigKeyNotFoundException if a value could not be found for the given key
    :return: The value found as a String
    """
    # print("debug:: configKey: " + configKey)
    # print("debug:: data: " + json.dumps(data, indent=4))
    if not isinstance(data, dict):
        raise ConfigKeyNotFoundException()
    if "." in configKey:
        parts = configKey.split(".", 1)
        curConfig = parts[0]
        keyLeft = parts[1]

        if curConfig not in data:
            raise ConfigKeyNotFoundException()

        return getConfigValRec(keyLeft, data[curConfig], format)
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
        # TODO:: secret dealings
    return result


def getConfigVal(configKey: str, data: dict = configData, format=True) -> str:
    try:
        return getConfigValRec(configKey, data, format)
    except ConfigKeyNotFoundException:
        print("ERROR: Config key not found: " + configKey, file=sys.stderr)
        exit(EXIT_BAD_CONFIG_KEY)



def setConfigVal(configKey: str, configVal: str, data: dict):
    """
    Updates the dict in place. Recursive
    :param configKey: The key in dot notation of the value to set
    :param configVal: The value to set
    :param data: The dict to update
    """
    if not isinstance(data, dict):
        raise ConfigKeyNotFoundException()
    if "." in configKey:
        parts = configKey.split(".", 1)
        curConfig = parts[0]
        keyLeft = parts[1]

        if curConfig not in data:
            data[curConfig] = {}
        setConfigVal(keyLeft, configVal, data[curConfig])
    else:
        # print("Debug: key: " + configKey)
        # print("Debug: val: " + configVal)
        # print("Debug: data: " + data)
        data[configKey] = configVal


args = argParser.parse_args()

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
elif args.s:
    configKeyToSet = args.s[0]
    configValToSet = args.s[1]
    configFile = args.s[2]

    if not configFile:
        configFile = DEFAULT_ADDENDUM_FILE
    if configFile == ".":
        configFile = MAIN_CONFIG_FILE
    else:
        configFile = ADD_CONFIG_DIR + "/" + configFile
    try:
        if not os.path.isfile(configFile):
            with open(configFile, 'x') as stream:
                stream.write('''
{
}
''')
    except OSError as e:
        print("Error: failed to set configuration value; can't create new config file. Try running as root. Error: ", e,
              file=sys.stderr)
        exit(EXIT_CONFIG_SET_ERR)

    curVals = readFile(configFile)

    try:
        setConfigVal(configKeyToSet, configValToSet, curVals)
    except ConfigKeyNotFoundException as e:
        print("Error: Bad config key given. Possibly trying to overwrite an existing value with a new object.",
              file=sys.stderr)
        exit(EXIT_BAD_CONFIG_KEY)

    jsonData = json.dumps(curVals, indent=4)
    try:
        with open(configFile, 'w') as stream:
            stream.write(jsonData)
    except OSError as e:
        print("Error: failed to set configuration value; can't write to config file. Try running as root. Error: ", e,
              file=sys.stderr)
        exit(EXIT_CONFIG_SET_ERR)

    print(jsonData)
else:
    print("No input given.")
    argParser.print_help()
