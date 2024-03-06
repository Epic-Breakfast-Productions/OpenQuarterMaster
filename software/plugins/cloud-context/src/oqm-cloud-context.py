#!/bin/python3

import json
import sys
import logging
import argparse
import argcomplete
import requests
from abc import ABC, abstractmethod
sys.path.append("/usr/lib/oqm/station-captain/")
from ConfigManager import *

log = logging.getLogger('oqm-cloud-context')
log.setLevel(logging.DEBUG)
CLOUD_CONTEXT_CONFIG_FILE = "80-plugin-cloud-context-pulled.json"


class CloudContext:
    pubDomainName: str = None


class CloudContextGetter(ABC):

    def setConfig(self):
        context = self.getContext()
        log.debug("Got context from provider: %s", json.dumps(context, default=lambda o: o.__dict__))
        if context.pubDomainName is None:
            raise Exception("No domain gotten from provider.")
        mainCM.setConfigValInFile("system.hostname", context.pubDomainName, CLOUD_CONTEXT_CONFIG_FILE)

    @abstractmethod
    def getContext(self) -> CloudContext:
        pass


class AwsCcg(CloudContextGetter):

    def __init__(self):
        self.url = mainCM.getConfigVal("plugins.cloud-context.aws.url")

    def getContext(self) -> CloudContext:
        contextOut = CloudContext()

        # Some interesting options: ("instance-id" "instance-type" "ami-id"
        #                  "placement/availability-zone" "placement/region"
        #                  "public-ipv4" "local-ipv4" "public-hostname"
        #                  "local-hostname" "mac" "security-groups")
        contextOut.pubDomainName = requests.get(self.url + "/public-hostname")

        return contextOut


argParser = argparse.ArgumentParser(
    prog="oqm-cloud-context",
    description="This script is a utility to pull context from a cloud provider to appropriately set configuration values automatically.",
    epilog=""
)
g = argParser.add_mutually_exclusive_group()
g.add_argument('-v', '--version', dest="v", action="store_true", help="Get this script's version")
g.add_argument('--pull-context', dest="pullContext", action="store_true", help="Pulls the context from the configured cloud provider.")

argcomplete.autocomplete(argParser)
args = argParser.parse_args()

if args.pullContext:
    cloudProvider = mainCM.getConfigVal("plugins.cloud-context.provider")
    if cloudProvider == "aws":
        log.info("Cloud provider set to AWS.")
        AwsCcg().setConfig()
    else:
        raise Exception("Bad value given for cloud provider: \""+cloudProvider+"\"")
