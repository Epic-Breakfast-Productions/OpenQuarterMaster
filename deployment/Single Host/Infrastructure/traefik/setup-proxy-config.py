#!/bin/python3
#
# Setup Proxy Config Script
# This script sets up the Traefik proxy configuration file for Open QuarterMaster.
#

import logging
import os
import argparse
import json
import sys
from os.path import basename
from time import sleep

from jinja2 import Environment, FileSystemLoader, select_autoescape
from requests.exceptions import HTTPError
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

sys.path.append("/usr/lib/oqm/station-captain/")
from ConfigManager import *
from LogUtils import *

LogUtils.setupLogging("infra-traefik-proxy-config.log", True)

log = LogUtils.setupLogger("main")
log.info("==== STARTING TRAEFIK CONFIG GENERATION ====")
log.info("Adding proxy config to traefik config.")

SCRIPT_VERSION = "1.0.0"
CONFIG_TEMPLATE_FILE = "/etc/oqm/serviceConfig/infra/traefik/traefik-dynamic-config-template.yaml.j2"
PROXY_CONFIG_DIR = "/etc/oqm/proxyConfig.d"
RESULT_CONFIG_FILE = "/tmp/oqm/serviceConfig/infra/traefik/config.d/dynamicConfig/traefik-dynamic-config.yaml"
TRAEFIK_CONTAINER_CERT_LOCATION = "/etc/traefik/certs/"

def updateTraefikConfig():
    log.info("Updating traefik config.")
    templateData = {
        "system": {
          "hostname": mainCM.getConfigVal("system.hostname"),
          "alternateSans": mainCM.getConfigVal("cert.additionalExternalSANs")
        },
        "defaultPath": False,
        "services": [],
        "certs": {
            "rootCa": TRAEFIK_CONTAINER_CERT_LOCATION + "rootCert.crt",
            "method": mainCM.getConfigVal("cert.externalDefault"),
            "default": None,
            "certList": [
                {
                    "key": TRAEFIK_CONTAINER_CERT_LOCATION + "privateKey.pem",
                    "cert": TRAEFIK_CONTAINER_CERT_LOCATION + "publicCert.crt"
                }
            ]
        }
    }

    if mainCM.getConfigVal("system.defaultUi.custom"):
        templateData["defaultPath"] = "/" + mainCM.getConfigVal("system.defaultUi.type") + "/" + mainCM.getConfigVal("system.defaultUi.name") + mainCM.getConfigVal("system.defaultUi.path")

    if mainCM.getConfigVal("cert.provided.enabled"):
        templateData["certs"]["certList"].append({
            "key": TRAEFIK_CONTAINER_CERT_LOCATION + "providedKey.pem",
            "cert": TRAEFIK_CONTAINER_CERT_LOCATION + "providedCert.crt"
        })

    match mainCM.getConfigVal("cert.externalDefault"):
        case "self":
            log.debug("Setting proxy config to self signed certs")
            templateData["certs"]["default"] = {
                "key": TRAEFIK_CONTAINER_CERT_LOCATION + "privateKey.pem",
                "cert": TRAEFIK_CONTAINER_CERT_LOCATION + "publicCert.crt"
            }
        case "acme":
            log.debug("Setting proxy config to acme certs")
        case "provided":
            log.debug("Setting proxy config to provided certs")
            templateData["certs"]["default"] = {
                "key": TRAEFIK_CONTAINER_CERT_LOCATION + "providedKey.pem",
                "cert": TRAEFIK_CONTAINER_CERT_LOCATION + "providedCert.crt"
            }
        case _:
            log.error("Value of external default was invalid.")




    for curProxyConfigFile in os.listdir(PROXY_CONFIG_DIR):
        try:
            if curProxyConfigFile.endswith(".json"):
                log.info("Cur proxy file: " + curProxyConfigFile)
                with open(PROXY_CONFIG_DIR + "/" + curProxyConfigFile, 'r') as stream:
                    curProxyConfig = json.load(stream)

                curProxyConfig["serviceName"] = curProxyConfig["type"] + "-" + curProxyConfig["name"]
                curProxyConfig["proxyPath"] = "/" + curProxyConfig["type"] + "/" + curProxyConfig["name"]

                if "internalBaseUri" not in curProxyConfig:
                    result, curProxyConfig["internalBaseUri"] = mainCM.waitForConfig(curProxyConfig["internalBaseUriConfig"])
                    if not result:
                        raise Exception("Could not find url for entry: " + curUiConfig["urlConfigKey"])

                if "preservePath" not in curProxyConfig:
                    curProxyConfig["preservePath"] = False
                if "stripPrefixes" not in curProxyConfig:
                    curProxyConfig["stripPrefixes"] = False

                log.debug("Using proxy config: %s", curProxyConfig)
                templateData["services"].append(curProxyConfig)
            else:
                continue
        except Exception as error:
            log.warning("Error processing proxy config file {} : {}".format(curProxyConfigFile, str(error)), exc_info=1)

    log.info("Done reading in proxy config files. Data: %s", templateData)

    env = Environment(
        loader=FileSystemLoader(os.path.dirname(CONFIG_TEMPLATE_FILE)),
        autoescape=select_autoescape()
    )
    template = env.get_template(os.path.basename(CONFIG_TEMPLATE_FILE))

    traefikConfig = template.render(templateData)

    os.makedirs(os.path.dirname(RESULT_CONFIG_FILE), exist_ok=True)
    with open(RESULT_CONFIG_FILE, "w") as f:
        f.write(traefikConfig)
    log.info("Finished writing new config file.")


class Handler(FileSystemEventHandler):
    def on_modified(self, event):
        log.info("Noted change in proxy configs. Updating proxy. File changed: %s", event.src_path)
        updateTraefikConfig()


argParser = argparse.ArgumentParser(
    prog="setup-proxy-config",
    description="This script is a utility to setup the dynamic configuration of Traefik.",
    epilog="Script version "+SCRIPT_VERSION+". With <3, EBP"
)
argParser.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
argParser.add_argument('--update-config', dest="updateConfig", action="store_true", help="Updates the OQM proxy config with the latest configuration.")
argParser.add_argument('--monitor-proxy-changes', dest="monitorChanges", action="store_true", help="Monitors the proxy config dir for changes, updates as needed.")
args = argParser.parse_args()

try:
    if args.v:
        print(SCRIPT_VERSION)
        exit()
    elif args.updateConfig:
        updateTraefikConfig()
    elif args.monitorChanges:
        log.info("Monitoring changes to keycloak clients in %s", PROXY_CONFIG_DIR)
        observer = Observer()
        observer.schedule(Handler(), PROXY_CONFIG_DIR)

        log.info("Running initial update of proxy.")
        updateTraefikConfig()
        log.info("Done running initial update.")
        log.info("Starting proxy change observer.")

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
    log.error("Exception thrown: ", e)
    exit(1)



log.info("==== END OF TRAEFIK CONFIG GENERATION ====")
