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

LogUtils.setupLogging("infra-homepage-config.log", True)

log = LogUtils.setupLogger("main")
log.info("==== STARTING HOMEPAGE CONFIG GENERATION ====")

SCRIPT_VERSION = "1.0.0"
TEMPLATE_DIR = "/etc/oqm/serviceConfig/infra/homepage/"
TEMPLATE_FILES = [
    "settings.yaml.j2",
    "bookmarks.yaml.j2",
    "services.yaml.j2",
    "widgets.yaml.j2"
]
UI_CONFIG_DIR = "/etc/oqm/ui.d/"
DESTINATION_CONFIG_DIR = "/tmp/oqm/serviceConfig/infra/homepage/config"

def updateHomepageConfig():
    log.info("Updating homepage config.")
    templateData = {
        "customization": mainCM.getConfigVal("infra.homepage.customization"),
        "services": {
            "Core": [],
            "Plugins": [],
            "Metrics": [],
            "Infra": []
        }
    }

    for curUiFile in os.listdir(UI_CONFIG_DIR):
        try:
            if curUiFile.endswith(".json"):
                log.info("Cur ui file: " + curUiFile)
                writeBack = False
                with open(UI_CONFIG_DIR + "/" + curUiFile, 'r') as stream:
                    curUiConfig = json.load(stream)

                if "url" not in curUiConfig or curUiConfig["url"] is None or mainCM.getConfigVal(curUiConfig["urlConfigKey"]) != curUiConfig["url"] :
                    result, curUiConfig["url"] = mainCM.waitForConfig(curUiConfig["urlConfigKey"])
                    if not result:
                        raise Exception("Could not find url for ui entry: " + curUiConfig["urlConfigKey"])
                    writeBack = True
                if "order" not in curUiConfig or curUiConfig["order"] is None:
                    curUiConfig["order"] = 999
                    writeBack = True
                if "monitorEndpoint" not in curUiConfig or curUiConfig["monitorEndpoint"] is None:
                    curUiConfig["monitorEndpoint"] = ""
                    writeBack = True

                log.debug("Using ui config: %s", curUiConfig)
                templateData["services"][curUiConfig["type"]].append(curUiConfig)

                if writeBack:
                    log.info("Updated entries in ui file. Updating original file.")
                    with open(UI_CONFIG_DIR + "/" + curUiFile, 'w') as stream:
                        json.dump(curUiConfig, stream, indent=4)
            else:
                continue
        except Exception as error:
            log.warning("Error processing ui config file {} : {}".format(curUiFile, str(error)), exc_info=1)

    log.info("Sorting services")
    for group, curServiceList in templateData["services"].items():
        templateData["services"][group] = sorted(curServiceList, key=lambda d: d['order'])

    log.info("Done reading in ui config files. Data: %s", templateData)

    env = Environment(
        loader=FileSystemLoader(TEMPLATE_DIR),
        autoescape=select_autoescape()
    )

    for curTemplateFile in TEMPLATE_FILES:
        resultFile = DESTINATION_CONFIG_DIR + "/" + curTemplateFile.replace(".j2", "")
        template = env.get_template(curTemplateFile)
        render = template.render(templateData)
        os.makedirs(DESTINATION_CONFIG_DIR, exist_ok=True)
        with open(resultFile, "w") as f:
            f.write(render)
        log.info("Finished writing new config file.")
    log.info("Finished writing new config files.")


class Handler(FileSystemEventHandler):
    def on_modified(self, event):
        log.info("Noted change in ui configs. Updating homepage. File changed: %s", event.src_path)
        updateHomepageConfig()


argParser = argparse.ArgumentParser(
    prog="setup-homepage-config",
    description="This script is a utility to setup the configuration of Homepage.",
    epilog="Script version "+SCRIPT_VERSION+". With <3, EBP"
)
argParser.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
argParser.add_argument('--update-config', dest="updateConfig", action="store_true", help="Updates the OQM homepage config with the latest configuration.")
argParser.add_argument('--monitor-changes', dest="monitorChanges", action="store_true", help="Monitors the ui config dir for changes, updates as needed.")
args = argParser.parse_args()

try:
    if args.v:
        print(SCRIPT_VERSION)
        exit()
    elif args.updateConfig:
        updateHomepageConfig()
    elif args.monitorChanges:
        log.info("Monitoring changes to ui entries in %s", UI_CONFIG_DIR)
        observer = Observer()
        observer.schedule(Handler(), UI_CONFIG_DIR)

        log.info("Running initial update of homepage.")
        updateHomepageConfig()
        log.info("Done running initial update.")
        log.info("Starting homepage change observer.")

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



log.info("==== END OF HOMEPAGE CONFIG GENERATION ====")
