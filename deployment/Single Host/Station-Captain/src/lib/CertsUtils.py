from enum import Enum
from ConfigManager import *
import logging
import subprocess


class CertsUtils:
    """

    """

    @staticmethod
    def generateSelfSignedCerts() -> bool:
        logging.info("Generating self-signed certs")
        # TODO

    @staticmethod
    def getLetsEncryptCerts() -> bool:
        logging.info("Getting Let's Encrypt certs")
        # TODO

    @staticmethod
    def regenCerts() -> bool:
        logging.info("Re-running cert generation utilities")
        # TODO:: depending on config, appropriately renew certs. pass when using provided certs




