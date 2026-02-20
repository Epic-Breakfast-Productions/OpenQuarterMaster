#!/bin/python3
import argparse
import os

import docker
from docker.models.containers import Container
import logging
import sys
import json
from time import sleep

from requests.exceptions import HTTPError
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

sys.path.append("/usr/lib/oqm/station-captain/")
from LogUtils import *

LogUtils.setupLogging("kc-realm-manager.log", True)
from ConfigManager import *

SCRIPT_VERSION = "2.0.0"
KC_CONTAINER_NAME = "oqm-infra-keycloak"
KC_ADM_SCRIPT = "/opt/keycloak/bin/kcadm.sh"
KC_REALM = "oqm"
KC_CLIENT_CONFIGS_DIR = "/etc/oqm/kcClients/"
KC_SYS_CLIENTS = [
    "account",
    "account-console",
    "admin-cli",
    "broker",
    "realm-management",
    "security-admin-console"
]
KC_SYS_ROLES = [
    "uma_authorization",
    "offline_access",
    "default-roles-oqm"
]
log = LogUtils.setupLogger("main")


def getClientConfigs() -> list:
    """
    Gets the list of client configs from the config dir.
    :return:
    """
    configList = os.listdir(KC_CLIENT_CONFIGS_DIR)
    configList = [KC_CLIENT_CONFIGS_DIR + i for i in configList]
    configList = [i for i in configList if os.path.isfile(i) and i.endswith(".json")]

    def fileToJson(jsonFile: str) -> object:
        with open(jsonFile) as file:
            return json.load(file)

    configList = [fileToJson(i) for i in configList]
    log.debug("Client configs found: %s", json.dumps(configList))

    #TODO:: validate client configs; no duplicate roles, clients, etc

    return configList


def getKcContainer() -> Container:
    """
    Gets the keycloak container to run things in.
    :return:
    """
    log.debug("Getting keycloak container.")
    client = docker.from_env()
    log.debug("Running containers: %s", client.containers.list())

    container = None
    while container is None:
        try:
            container = client.containers.get(KC_CONTAINER_NAME)
        except HTTPError as e:
            log.warn("HTTP error from trying to get keycloak container: %s", e)
            sleep(5)
    log.info("Got keycloak container.")
    return container


def setupAdminConfig(kcContainer: Container | None = None):
    """
    Sets up the admin client within the keycloak container.
    :param kcContainer:
    :return:
    """
    log.info("Setting up keycloak admin config for future calls.")
    if kcContainer is None:
        kcContainer = getKcContainer()

    # Set OQM Truststore
    if mainCM.getConfigVal("cert.externalDefault") != "acme":
        log.info("Setting up oqm trust store for KC admin.")

        runResult = kcContainer.exec_run(
            [
                KC_ADM_SCRIPT, "config", "truststore",
                "--trustpass", mainCM.getConfigVal("cert.trustStore.files.selfSigned.p12Password"),
                mainCM.getConfigVal("cert.trustStore.files.selfSigned.p12")
            ])

        if runResult.exit_code != 0:
            log.error("Failed to setup oqm trust store for KC admin: %s", runResult.output)
            raise ChildProcessError("Failed to setup oqm trust store for KC admin")
    else:
        # TODO: #998 support other ACME providers
        log.info("Skipping setting up oqm trust store for KC admin due to using Let's Encrypt.")

    runResult = kcContainer.exec_run(
        [
            KC_ADM_SCRIPT, "config", "credentials",
            "--server", mainCM.getConfigVal("infra.keycloak.externalBaseUri"),
            "--realm", "master",  # admin user exists in master realm
            "--user", mainCM.getConfigVal("infra.keycloak.adminUser"),
            "--password", mainCM.getConfigVal("infra.keycloak.adminPass")
        ])
    if runResult.exit_code != 0:
        log.error("Failed to setup kc admin credentials: " + str(runResult.output))
        raise ChildProcessError("Failed to setup admin credentials")
    log.debug("Setting up KC creds output: %s", runResult.output)


def getAllClientData(kcContainer: Container | None = None):
    """
    Gets a list of all client data in the realm.

    Example:

    [
        {
            "id": "931b5ce4-c880-4669-909b-816620e6c03b",
            "clientId": "oqm-base-station",
            "name": "OQM Base Station",
            "description": "The client for OQM's Base Station main component.",
            "surrogateAuthRequired": false,
            "enabled": true,
            "alwaysDisplayInConsole": true,
            "clientAuthenticatorType": "client-secret",
            "secret": "qM_mXmlPZGHUmx1pGaEDRhiy2wLXQpbB",
            "redirectUris": [
              "*"
            ],
            "webOrigins": [],
            "notBefore": 0,
            "bearerOnly": false,
            "consentRequired": false,
            "standardFlowEnabled": true,
            "implicitFlowEnabled": false,
            "directAccessGrantsEnabled": false,
            "serviceAccountsEnabled": true,
            "publicClient": false,
            "frontchannelLogout": false,
            "protocol": "openid-connect",
            "attributes": {
              "realm_client": "false"
            },
            "authenticationFlowBindingOverrides": {},
            "fullScopeAllowed": true,
            "nodeReRegistrationTimeout": -1,
            "defaultClientScopes": [
              "web-origins",
              "service_account",
              "acr",
              "roles",
              "profile",
              "microprofile-jwt",
              "basic",
              "email"
            ],
            "optionalClientScopes": [
              "address",
              "phone",
              "offline_access"
            ],
            "access": {
              "view": true,
              "configure": true,
              "manage": true
            }
          },
          ...
    ]

    :param kcContainer:
    :return:
    """
    if kcContainer is None:
        kcContainer = getKcContainer()
    runResult = kcContainer.exec_run(
        [
            KC_ADM_SCRIPT, "get", "clients", "-r", KC_REALM
        ])
    if runResult.exit_code != 0:
        log.error("Failed to get keycloak clients: %s", runResult.output)
        raise ChildProcessError("Failed to get keycloak clients")

    output = json.loads(runResult.output)
    output = [output for output in output if output["clientId"] not in KC_SYS_CLIENTS]

    return output


def getClientId(clientName: str, kcContainer: Container | None = None) -> str | None:
    """
    Gets the client id for the given client name
    :param clientName:
    :param kcContainer:
    :return:
    """
    if kcContainer is None:
        kcContainer = getKcContainer()
    runResult = kcContainer.exec_run([
        KC_ADM_SCRIPT, "get", "clients", "-r", KC_REALM, "--fields", "id,clientId"
    ])
    if runResult.exit_code != 0:
        log.error("Failed to get keycloak clients: %s", runResult.output)
        raise ChildProcessError("Failed to get keycloak clients")
    log.debug("Clients list: %s", runResult.output)
    allClientData = json.loads(runResult.output)
    for curClient in allClientData:
        if clientName == curClient["clientId"]:
            return curClient["id"]
    return None


# def getClientData(clientName:str, kcContainer: Container | None = None) -> str | None:
#     """
#     Gets the client data of a specific client
#     :param clientName:
#     :param kcContainer:
#     :return:
#     """
#     allClientData = getAllClientData(kcContainer)
#     for curClient in allClientData:
#         if clientName == curClient["clientId"]:
#             return curClient["id"]
#     return None

def processRole(kcContainer: Container, roleDef: dict) -> (bool, str):
    """
    Processes a role definition, integrating it into Keycloak.
    :param kcContainer:
    :param roleDef:
    :return:
    """
    # TODO::
    return True, ""

def processRoles(kcContainer: Container, roleDef: list[dict]) -> (bool, str):
    log.info("Processing roles")
    for role in roleDef:
        success, msg = processRole(kcContainer, role)
    # TODO:: error check
    log.info("Done processing roles")
    return True, ""

def processClient(kcContainer: Container, roleDef: dict) -> (bool, str):
    # TODO:: rework into parts, update for new schema

    log.info("Adding client %s", roleDef['clientName'])
    # TODO:: validate object data
    clientName = roleDef['clientName']
    mainCM.setConfigValInFile("infra.keycloak.clientSecrets." + clientName, "<secret>", "11-keycloak-clients.json")
    mainCM.rereadConfigData()
    newClientJson = {
        "clientId": roleDef['clientName'],
        "name": roleDef['displayName'],
        "secret": mainCM.getConfigVal("infra.keycloak.clientSecrets." + clientName),
        "description": roleDef['description'],
        "redirectUris": ["*"],
        "alwaysDisplayInConsole": True,
        "serviceAccountsEnabled": True
    }
    # TODO:: if exists, update instead of create
    runResult = kcContainer.exec_run([
        KC_ADM_SCRIPT,
        "create",
        "clients",
        "-r", KC_REALM,
        "-b", json.dumps(newClientJson)
    ])
    if runResult.exit_code != 0:
        log.error("Failed to add new client: %s", runResult.output)
        raise ChildProcessError("Failed to add new client")
    clientId = getClientId(roleDef['clientName'])

    log.debug("Client id: %s", clientId)
    # TODO:: add roles

    # if "serviceAccount" in curClient and curClient['serviceAccount']['enabled']:
    #     runResult = kcContainer.exec_run([
    #         KC_ADM_SCRIPT,
    #
    #         "-r", KC_REALM,
    #     ])
    return True, ""

def processClients(kcContainer: Container, clientsDefs: list[dict]) -> (bool, str):
    log.info("Processing clients")
    for clientDef in clientsDefs:
        success, msg = processClient(kcContainer, clientDef)
    # TODO:: error check
    log.info("Done processing clients")
    return True, ""

def processServiceIntegration(kcContainer: Container, integrationDef: dict) -> (bool, str):
    """
    Processes a service integration definition by adding a new client to Keycloak.
    :param kcContainer: The Keycloak container instance.
    :param integrationDef: The integration definition dictionary.
    :return: A tuple containing a boolean indicating success and a message.
    """
    log.info("Processing integration: %s", integrationDef['service'])

    success, msg = processRoles(kcContainer, integrationDef['roles'])
    # TODO:: error check

    success, msg = (kcContainer, integrationDef['clients'])
    # TODO:: error check


    return True, ""

def updateRealmSettings(kcContainer: Container):
    log.info("Updating realm settings.")
    runResult = kcContainer.exec_run([
        KC_ADM_SCRIPT,
        "update",
        "realms/" + KC_REALM,
        "-s", "registrationAllowed=" + str(mainCM.getConfigVal("infra.keycloak.options.userSelfRegistration"))
    ])
    if runResult.exit_code != 0:
        log.error("Failed to set registration allowed: %s", runResult.output)
        raise ChildProcessError("Failed to set registration allowed")
    log.debug("Setting up KC registration allowed: %s", runResult.output)
    log.info("Done updating realm settings.")

def updateKc():
    """
    "Main" method for reloading and applying keycloak settings
    :return:
    """
    log.info("Updating Keycloak Realm")
    kcContainer = getKcContainer()
    setupAdminConfig(kcContainer)

    updateRealmSettings(kcContainer)

    # Remove all clients not in the set brought in by keycloak
    # log.info("Removing all clients before re-creation")
    # allClientData = getAllClientData(kcContainer)
    # for curClient in allClientData:
    #     if curClient["clientId"] in KC_SYS_CLIENTS:
    #         continue
    #     log.info("Removing client to re-create: %s", curClient["clientId"])
    #     clientId = getClientId(curClient["clientId"])
    #     log.debug("Client id: %s", clientId)
    #     runResult = kcContainer.exec_run([
    #         KC_ADM_SCRIPT,
    #         "delete",
    #         "clients/" + clientId,
    #         "-r", KC_REALM
    #     ])
    #     if runResult.exit_code != 0:
    #         log.error("Failed to remove client: %s", runResult.output)
    #         raise ChildProcessError("Failed to remove client")

    clientConfigs = getClientConfigs()

    log.info("Creating clients from config.")
    for curClient in clientConfigs:
        result, msg = processServiceIntegration(kcContainer, curClient)

    log.info("Done updating realm.")


class Handler(FileSystemEventHandler):
    def on_modified(self, event):
        log.info("Noted change in kc client configs. Updating realms. File changed: %s", event.src_path)
        updateKc()


argParser = argparse.ArgumentParser(
    prog="kc-realm-manager",
    description="This script is a utility to help manage OQM's Keycloak installation.",
    epilog="Script version " + SCRIPT_VERSION + ". With <3, EBP"
)
argParser.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
argParser.add_argument('--update-realm', dest="updateRealm", action="store_true", help="Updates the OQM realm with the latest configuration.")
argParser.add_argument('--monitor-client-changes', dest="monitorChanges", action="store_true", help="Monitors the clients config dir for changes, updates as needed.")
args = argParser.parse_args()

try:
    if args.v:
        print(SCRIPT_VERSION)
        exit()
    elif args.updateRealm:
        updateKc()
    elif args.monitorChanges:
        log.info("Monitoring changes to keycloak clients in %s", KC_CLIENT_CONFIGS_DIR)
        observer = Observer()
        observer.schedule(Handler(), KC_CLIENT_CONFIGS_DIR)

        log.info("Running initial update of keycloak clients.")
        updateKc()
        log.info("Done running initial update.")
        log.info("Starting client change observer.")

        observer.start()

        log.info("Started observer.")

        try:
            while True:
                sleep(5)
        except KeyboardInterrupt:
            observer.stop()
        log.info("STOPPING monitoring directory for changes.")
        observer.join()
    else:
        argParser.print_usage()
except Exception as e:
    log.error("Exception thrown: {e}", exc_info=True)
    exit(1)
