from enum import Enum
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
    SERVICE_ALL_INFRA = "open\\x2bquarter\\x2bmaster\\x2dinfra*"
    SERVICE_ALL_CORE = "open\\x2bquarter\\x2bmaster\\x2dcore*"
    SERVICE_ALL = "open\\x2bquarter\\x2bmaster*"

    @staticmethod
    def doServiceCommand(command: ServiceStateCommand, service: str) -> bool:
        logging.info("Performing %s command on service %s", command, service)
        result = subprocess.run(["systemctl", command.name, service], shell=False, capture_output=True, text=True, check=False)

        if result.returncode != 0:
            logging.warning("Command was unsuccessful. Error code: {0}", result.returncode)
            logging.debug("Erring command stderr: {0}", result.stderr)
            return False
        logging.info("Command was successful.")
        return True




