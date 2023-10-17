import logging
import os
import shutil
from enum import Enum, StrEnum
from ConfigManager import *
from ServiceUtils import *


class DataToRemove(StrEnum):
    CORE = 'core'


class DataUtils:

    @staticmethod
    def clearAllData() -> bool:
        ServiceUtils.doServiceCommand(
            ServiceStateCommand.stop,
            ServiceUtils.SERVICE_ALL
        )

        logging.info("Clearing ALL Data.")

        try:
            shutil.rmtree(mainCM.getConfigVal("system.dataDir"))
        except Exception as e:
            logging.error("FAILED to clear data: %s", e)
            return False

        logging.info("Done Clearing ALL Data.")

        ServiceUtils.doServiceCommand(
            ServiceStateCommand.start,
            ServiceUtils.SERVICE_ALL
        )
        return True
