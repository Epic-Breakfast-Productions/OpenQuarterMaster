#!/bin/python3
import argparse
import os
from filelock import FileLock
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
MUTEX_FILE="/tmp/oqm-kc-ream-manager.lock"
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


def getServiceConfigs() -> list:
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

    serviceNames = []
    roleNames = []
    clientIds = []
    for curConfig in configList:
        curName = curConfig['name']

        if curName in serviceNames:
            raise Exception("Duplicate service name found: " + curName)
        serviceNames.append(curName)

        for curRole in curConfig['roles']:
            curRoleName = curRole['name']
            if curRoleName in roleNames:
                raise Exception("Duplicate role name found: " + curRoleName)
            roleNames.append(curRoleName)

        for curClient in curConfig['clients']:
            curClientId = curClient['clientId']
            if curClientId in clientIds:
                raise Exception("Duplicate client name found: " + curClientId)
            clientIds.append(curClientId)

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


def getAllClientData(kcContainer: Container | None = None) -> list[dict]:
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

def getClientData(clientId: str, kcContainer: Container | None = None) -> dict | None:
    allClients = getAllClientData(kcContainer)
    for curRole in allClients:
        if clientId == curRole["clientId"]:
            return curRole
    return None

def clientExists(clientId: str, kcContainer: Container | None = None) -> bool:
    return getClientData(clientId, kcContainer) is not None

def getAllRoleData(kcContainer: Container | None = None) -> list[dict]:
    """
    Example:
    [
        {
            "id": "abc50c50-5c5e-40c4-8a02-36b393e37f34",
            "name": "uma_authorization",
            "description": "${role_uma_authorization}",
            "composite": false,
            "clientRole": false,
            "containerId": "72bd1a2f-d711-48ce-b65b-0ad7107e8d56"
        }
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

    output = json.loads(runResult.output)
    output = [output for output in output if output["clientId"] not in KC_SYS_CLIENTS]

    return output

def getRoleData(roleName: str, kcContainer: Container | None = None) -> dict | None:
    allRoles = getAllRoleData(kcContainer)
    for curRole in allRoles:
        if roleName == curRole["name"]:
            return curRole
    return None

def roleExists(roleName: str, kcContainer: Container | None = None) -> bool:
    return getRoleData(roleName, kcContainer) is not None

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
    kcRoleData = {
        "name": roleDef['name'],
        "description": roleDef.get('description', ""),
    }

    if roleExists(kcRoleData['name'], kcContainer):
        log.info("Role %s already exists, updating.", kcRoleData['name'])

        runResult = kcContainer.exec_run([
            KC_ADM_SCRIPT, "update", "roles/"+kcRoleData['name'], "-r", KC_REALM, "-b", json.dumps(kcRoleData)
        ])
        if runResult.exit_code != 0:
            log.error("Failed to create keycloak role: %s", runResult.output)
            raise ChildProcessError("Failed to create keycloak role: " + kcRoleData['name'])
    else:
        log.info("Role %s does not exist, creating.", kcRoleData['name'])
        runResult = kcContainer.exec_run([
            KC_ADM_SCRIPT, "create", "roles", "-r", KC_REALM, "-b", json.dumps(kcRoleData)
        ])
        if runResult.exit_code != 0:
            log.error("Failed to create keycloak role: %s", runResult.output)
            raise ChildProcessError("Failed to create keycloak role: " + kcRoleData['name'])

    # TODO:: group membership
    # TODO:: respect default; apply to users automatically

    return True, ""

def processRoles(kcContainer: Container, roleDef: list[dict]) -> (bool, str):
    log.info("Processing roles")

    customRoles = []
    for role in roleDef:
        customRoles.append(role['name'])
        success, msg = processRole(kcContainer, role)
    # TODO:: error check
    #TODO:: remove roles not in config

    log.info("Done processing roles")
    return True, ""

def processClient(kcContainer: Container, clientDef: dict) -> (bool, str):
    log.info("Adding client %s", clientDef['clientId'])
    clientId = clientDef['clientId']
    #ensure secret config exists
    mainCM.setConfigValInFile("infra.keycloak.clientSecrets." + clientId, "<secret>", "11-keycloak-clients.json")
    mainCM.rereadConfigData()

    kcClientData = {
        "clientId": clientId,
        "name": clientDef['displayName'],
        "secret": mainCM.getConfigVal("infra.keycloak.clientSecrets." + clientId),
        "description": clientDef['description'],
        "redirectUris": ["*"],
        "alwaysDisplayInConsole": True,
        "serviceAccountsEnabled": True
    }

    if clientExists(kcClientData['clientId'], kcContainer):
        log.info("Client %s already exists, updating.", kcClientData['clientId'])

        runResult = kcContainer.exec_run([
            KC_ADM_SCRIPT, "update", "clients/"+kcClientData['clientId'], "-r", KC_REALM, "-b", json.dumps(kcClientData)
        ])
        if runResult.exit_code != 0:
            log.error("Failed to create keycloak client: %s", runResult.output)
            raise ChildProcessError("Failed to update keycloak client: " + kcClientData['clientId'])
    else:
        log.info("Client %s does not exist, creating.", kcClientData['clientId'])
        runResult = kcContainer.exec_run([
            KC_ADM_SCRIPT, "create", "clients", "-r", KC_REALM, "-b", json.dumps(kcClientData)
        ])
        if runResult.exit_code != 0:
            log.error("Failed to create keycloak client: %s", runResult.output)
            raise ChildProcessError("Failed to create keycloak client: " + kcClientData['clientId'])

    # TODO:: validate object data
    # TODO:: add roles?
    return True, ""

def processClients(kcContainer: Container, clientsDefs: list[dict]) -> (bool, str):
    log.info("Processing clients")
    customClients = []
    for clientDef in clientsDefs:
        customClients.append(clientDef['clientId'])
        success, msg = processClient(kcContainer, clientDef)
    # TODO:: error check
    # TODO:: remove clients not in config
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

    success, msg = processClients(kcContainer, integrationDef['clients'])
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
    log.info("Acquiring lock for Keycloak Realm update.")
    with FileLock(MUTEX_FILE):
        log.info("Updating Keycloak Realm")
        try:
            # Init
            kcContainer = getKcContainer()
            setupAdminConfig(kcContainer)

            # Update general settings from config
            updateRealmSettings(kcContainer)

            # Process service integrations
            serviceConfigs = getServiceConfigs()
            for curService in serviceConfigs:
                result, msg = processServiceIntegration(kcContainer, curService)
            log.info("Done updating realm.")
        except Exception as e:
            log.error("Exception thrown during update: {e}", exc_info=True)
    log.info("Released lock for Keycloak Realm update.")

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
