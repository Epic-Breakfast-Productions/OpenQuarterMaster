import re
from enum import Enum
from typing import Union

from ConfigManager import *
import logging
import subprocess


class ServiceStateCommand(Enum):
    start = 1,
    stop = 2,
    restart = 3


class ServiceUtils:
    """

    """
    SERVICE_ALL_INFRA = "oqm-infra-*"
    SERVICE_ALL_CORE = "oqm-core-*"
    SERVICE_ALL = "oqm-*"

    @staticmethod
    def doServiceCommand(command: ServiceStateCommand, service: str) -> bool:
        logging.info("Performing %s command on service %s", command, service)
        args = ["systemctl", command.name, service]
        if "*" in service:
            args.append("--all")
        result = subprocess.run(args, shell=False, capture_output=True, text=True, check=False)

        if result.returncode != 0:
            # logging.warning("Command was unsuccessful. Error code: {0}", result.returncode)
            # logging.debug("Erring command stderr: {0}", result.stderr)
            return False
        logging.info("%s Command was successful on %s", command, service)
        return True

    @staticmethod
    def getServiceNames(serviceFilter: str = SERVICE_ALL) -> (bool, [str]):
        logging.info("Getting service names based on filter: %s", serviceFilter)
        result = subprocess.run(["systemctl", "list-units", "--no-legend", "--all", serviceFilter], shell=False, capture_output=True, text=True, check=False)

        if result.returncode != 0:
            logging.warning("Command was unsuccessful. Error code: {0}", result.returncode)
            logging.debug("Erring command stderr: {0}", result.stderr)
            return False, result.stderr

        output = []
        for curRawService in re.split(r'\n', result.stdout):
            curService = re.split(r' ', curRawService[2:])[0].strip()
            if len(curService):
                output.append(curService)
        logging.debug("Got OQM services: %s", output)

        return True, output

    # -q
    # --no-legend
    #
