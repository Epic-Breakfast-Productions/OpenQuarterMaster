import os
import shutil
from ConfigManager import *
from ServiceUtils import *
from LogUtils import *

log = LogUtils.setupLogger(__name__)

class DataUtils:

    @staticmethod
    def clearAllData() -> bool:
        ServiceUtils.doServiceCommand(
            ServiceStateCommand.stop,
            ServiceUtils.SERVICE_ALL
        )

        log.info("Clearing ALL Data.")

        try:
            shutil.rmtree(mainCM.getConfigVal("system.dataDir"))
        except Exception as e:
            log.error("FAILED to clear data: %s", e)
            return False

        log.info("Done Clearing ALL Data.")

        ServiceUtils.doServiceCommand(
            ServiceStateCommand.start,
            ServiceUtils.SERVICE_ALL
        )
        return True
