import os
import shutil
from ConfigManager import *
from ServiceUtils import *
from LogUtils import *


class DataUtils:
    log = LogUtils.setupLogger("DataUtils")

    @staticmethod
    def clearAllData() -> bool:
        ServiceUtils.doServiceCommand(
            ServiceStateCommand.stop,
            ServiceUtils.SERVICE_ALL
        )

        DataUtils.log.info("Clearing ALL Data.")

        try:
            shutil.rmtree(mainCM.getConfigVal("system.dataDir"))
        except Exception as e:
            DataUtils.log.error("FAILED to clear data: %s", e)
            return False

        DataUtils.log.info("Done Clearing ALL Data.")

        ServiceUtils.doServiceCommand(
            ServiceStateCommand.start,
            ServiceUtils.SERVICE_ALL
        )
        return True
