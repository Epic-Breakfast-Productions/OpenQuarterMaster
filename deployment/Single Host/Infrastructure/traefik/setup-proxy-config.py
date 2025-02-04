#!/bin/python3
#
# Setup Proxy Config Script
# This script sets up the Traefik proxy configuration file for Open QuarterMaster.
#

import logging
import os
import json
import sys
from os.path import basename

from jinja2 import Environment, FileSystemLoader, select_autoescape

sys.path.append("/usr/lib/oqm/station-captain/")
from ConfigManager import *
from LogUtils import *

LogUtils.setupLogging("infra-traefik-proxy-config.log", "--verbose" in sys.argv)

log = LogUtils.setupLogger("main")
log.info("==== STARTING TRAEFIK CONFIG GENERATION ====")
log.info("Adding proxy config to traefik config.")

CONFIG_TEMPLATE_FILE = "/etc/oqm/serviceConfig/infra/traefik/traefik-dynamic-config-template.yaml.j2"
PROXY_CONFIG_DIR = "/etc/oqm/proxyConfig.d"
RESULT_CONFIG_FILE = "/tmp/oqm/serviceConfig/infra/traefik/config.d/dynamicConfig/traefik-dynamic-config.yaml"
TRAEFIK_CONTAINER_CERT_LOCATION = "/etc/traefik/certs/"

templateData = {
    "system": {
      "hostname": mainCM.getConfigVal("system.hostname")
    },
    "defaultPath": "/" + mainCM.getConfigVal("system.defaultUi.type") + "/" + mainCM.getConfigVal("system.defaultUi.name") + mainCM.getConfigVal("system.defaultUi.path"),
    "services": [],
    "certs": {
        "rootCa": TRAEFIK_CONTAINER_CERT_LOCATION + "rootCert.crt",
        "method": "self",
        "default": {
            "key": TRAEFIK_CONTAINER_CERT_LOCATION + "privateKey.pem",
            "cert": TRAEFIK_CONTAINER_CERT_LOCATION + "publicCert.crt"
        },
        "certList": [
            {
                "key": TRAEFIK_CONTAINER_CERT_LOCATION + "privateKey.pem",
                "cert": TRAEFIK_CONTAINER_CERT_LOCATION + "publicCert.crt"
            }
        ]
    }
}

for curProxyConfigFile in os.listdir(PROXY_CONFIG_DIR):
    if curProxyConfigFile.endswith(".json"):
        log.info("Cur proxy file: " + curProxyConfigFile)
        with open(PROXY_CONFIG_DIR + "/" + curProxyConfigFile, 'r') as stream:
            curProxyConfig = json.load(stream)

        curProxyConfig["serviceName"] = curProxyConfig["type"] + "-" + curProxyConfig["name"]
        curProxyConfig["proxyPath"] = "/" + curProxyConfig["type"] + "/" + curProxyConfig["name"]

        if "internalBaseUri" not in curProxyConfig:
            curProxyConfig["internalBaseUri"] = mainCM.getConfigVal(curProxyConfig["internalBaseUriConfig"])
        if "preservePath" not in curProxyConfig:
            curProxyConfig["preservePath"] = False
        if "stripPrefixes" not in curProxyConfig:
            curProxyConfig["stripPrefixes"] = False

        log.info("Using proxy config: %s", curProxyConfig)
        templateData["services"].append(curProxyConfig)
    else:
        continue

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

log.info("==== END OF TRAEFIK CONFIG GENERATION ====")
