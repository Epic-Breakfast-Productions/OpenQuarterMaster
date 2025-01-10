#!/bin/python3

import json
import sys
import logging
import argparse
import argcomplete
import requests
import boto3
from botocore.config import Config
from abc import ABC, abstractmethod
sys.path.append("/usr/lib/oqm/station-captain/")
from LogUtils import *
LogUtils.setupLogging("snapshot-backup.log", "--verbose" in sys.argv)

from ConfigManager import *

log = logging.getLogger('main')
CLOUD_CONTEXT_CONFIG_FILE = "80-plugin-snapshot-backup.json"

class SyncMethod:
    @staticmethod
    @abstractmethod
    def sync(self)-> (bool, str):
        pass  # This is an abstract method, no implementation here.

class ObjStorageSync(SyncMethod):

    @staticmethod
    def sync(self)-> (bool, str):
        return False, "Not yet implemented"









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
