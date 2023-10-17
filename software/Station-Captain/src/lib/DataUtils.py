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
    def clearAllData():
        ServiceUtils.doServiceCommand(
            ServiceStateCommand.stop,
            ServiceUtils.SERVICE_ALL
        )

        logging.info("Clearing ALL Data.")

        # TODO
        # shutil.rmtree(mainCM.getConfigVal(""))

        logging.info("Done Clearing ALL Data.")

        ServiceUtils.doServiceCommand(
            ServiceStateCommand.start,
            ServiceUtils.SERVICE_ALL
        )
