from enum import Enum
from ConfigManager import *
from ServiceUtils import *
import logging
import subprocess


class SnapshotUtils:
    """

    """

    @staticmethod
    def performSnapshot() -> bool:
        logging.info("Performing snapshot.")
        ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

        # TODO:: run snapshot

        ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)
        logging.info("Done Performing snapshot.")

    @staticmethod
    def restoreFromSnapshot(snapshotFile:str) -> bool:
        logging.info("Performing snapshot Restore.")
        ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

        # TODO:: run snapshot

        ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)
        logging.info("Done Performing snapshot Restore.")





