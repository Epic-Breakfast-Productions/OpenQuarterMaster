#!/bin/python3
import argparse
import os

import docker
from docker.models.containers import Container
import logging
import sys
import json
sys.path.append("/usr/lib/oqm/station-captain/")
from ConfigManager import *

SCRIPT_VERSION = "1.0.0"
KC_CONTAINER_NAME = "oqm_infra_keycloak"
KC_ADM_SCRIPT = "/opt/keycloak/bin/kcadm.sh"
KC_REALM = "oqm"
KC_CLIENT_CONFIGS_DIR = "/etc/oqm/kcClient/"
KC_DEFAULT_CLIENTS = [
    "account",
    "account-console",
    "admin-cli",
    "broker",
    "realm-management",
    "security-admin-console"
]
logging.basicConfig(level=logging.DEBUG)


def getClientConfigs() -> list:
    configList = os.listdir(KC_CLIENT_CONFIGS_DIR)
    configList = [KC_CLIENT_CONFIGS_DIR + i for i in configList]
    configList = [i for i in configList if os.path.isfile(i) and i.endswith(".json")]

    def fileToJson(jsonFile : str) -> object:
        with open(jsonFile) as file:
            return json.load(file)

    configList = [fileToJson(i) for i in configList]
    logging.debug("Client configs found: %s", json.dumps(configList))
    return configList


def getKcContainer() -> Container:
    client = docker.from_env()
    return client.containers.get(KC_CONTAINER_NAME)


def setupAdminConfig(kcContainer: Container | None = None):
    logging.info("Setting up keycloak admin config for future calls.")
    if kcContainer is None:
        kcContainer = getKcContainer()

    runResult = kcContainer.exec_run(
        [
            KC_ADM_SCRIPT, "config", "credentials", "--server", "http://127.0.0.1:8080",
            "--realm", "master",  # admin user exists in master realm
            "--user", mainCM.getConfigVal("infra.keycloak.adminUser"),
            "--password", mainCM.getConfigVal("infra.keycloak.adminPass")
        ])
    if runResult.exit_code != 0:
        logging.error("Failed to setup kc admin credentials: %s", runResult.output)
        raise ChildProcessError("Failed to setup admin credentials")
    logging.debug("Setting up KC creds output: %s", runResult.output)


def getAllClientData(kcContainer: Container | None = None):
    if kcContainer is None:
        kcContainer = getKcContainer()
    runResult = kcContainer.exec_run(
        [
            KC_ADM_SCRIPT, "get", "clients", "-r", KC_REALM
        ])
    if runResult.exit_code != 0:
        logging.error("Failed to get keycloak clients: %s", runResult.output)
        raise ChildProcessError("Failed to get keycloak clients")
    return json.loads(runResult.output)


def getClientId(clientName: str, kcContainer: Container | None = None) -> str | None:
    if kcContainer is None:
        kcContainer = getKcContainer()
    runResult = kcContainer.exec_run([
            KC_ADM_SCRIPT, "get", "clients", "-r", KC_REALM, "--fields", "id,clientId"
        ])
    if runResult.exit_code != 0:
        logging.error("Failed to get keycloak clients: %s", runResult.output)
        raise ChildProcessError("Failed to get keycloak clients")
    logging.debug("Clients list: %s", runResult.output)
    allClientData = json.loads(runResult.output)
    for curClient in allClientData:
        if clientName == curClient["clientId"]:
            return curClient["id"]
    return None


def getClientData(clientName:str, kcContainer: Container | None = None) -> str | None:
    allClientData = getAllClientData(kcContainer)
    for curClient in allClientData:
        if clientName == curClient["clientId"]:
            return curClient["id"]
    return None


def updateKc():
    kcContainer = getKcContainer()
    setupAdminConfig(kcContainer)

    runResult = kcContainer.exec_run([
            KC_ADM_SCRIPT,
            "update",
            "realms/"+KC_REALM,
            "-s", "registrationAllowed=" + str(mainCM.getConfigVal("infra.keycloak.options.userSelfRegistration"))
        ])
    if runResult.exit_code != 0:
        logging.error("Failed to set registration allowed: %s", runResult.output)
        raise ChildProcessError("Failed to set registration allowed")
    logging.debug("Setting up KC registration allowed: %s", runResult.output)

    for curClient in getClientConfigs():
        logging.info("Processing client %s", curClient['clientName'])
        # TODO:: validate object data
        clientName = curClient['clientName']
        clientId = getClientId(clientName)
        if clientId is None:
            logging.info("Client not currently present. Adding.")
            mainCM.setConfigValInFile("infra.keycloak.clientSecrets."+clientName, "<secret>", "11-keycloak-clients.json")
            newClientJson = {
                "clientId": curClient['clientName'],
                "name": curClient['displayName'],
                "secret": mainCM.getConfigVal("infra.keycloak.clientSecrets."+clientName),
                "description": curClient['description'],
                "redirectUris": ["*"]
            }
            runResult = kcContainer.exec_run([
                KC_ADM_SCRIPT,
                "create",
                "clients",
                "-r", KC_REALM,
                "-b", json.dumps(newClientJson)
            ])
            if runResult.exit_code != 0:
                logging.error("Failed to add new client: %s", runResult.output)
                raise ChildProcessError("Failed to add new client")
            clientId = getClientId(curClient['clientName'])

        logging.debug("Client id: %s", clientId)
        # TODO:: update client information, add roles


argParser = argparse.ArgumentParser(
    prog="kc-realm-manager",
    description="This script is a utility to help manage OQM's Keycloak installation.",
    epilog="Script version "+SCRIPT_VERSION+". With <3, EBP"
)
argParser.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
argParser.add_argument('--update-realm', dest="updateRealm", action="store_true", help="Updates the OQM realm with the latest configuration.")
args = argParser.parse_args()

if args.v:
    print(SCRIPT_VERSION)
    exit()
elif args.updateRealm:
    updateKc()
else:
    argParser.print_usage()