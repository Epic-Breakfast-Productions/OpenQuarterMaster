import os
from enum import Enum
from ConfigManager import *
from LogUtils import *
import os
import stat



class CronFrequency(Enum):
    hourly = 1
    daily = 2
    weekly = 3
    monthly = 4

    @staticmethod
    def getFreqListStr()->str:
        return ", ".join(CronFrequency._member_names_)


class CronUtils:
    log = LogUtils.setupLogger("CronUtils")

    @staticmethod
    def getFileName(name: str):
        return "oqm-" + name

    @staticmethod
    def getFileDir(frequency: CronFrequency, fileName: str = None):
        output = "/etc/cron.{}".format(frequency.name)

        if fileName is not None:
            output += "/" + fileName
        return output

    @staticmethod
    def disableCron(name: str):
        CronUtils.log.info("Disabling cron " + name)
        fileName = CronUtils.getFileName(name)

        for curFrequency in CronFrequency:
            curFile = CronUtils.getFileDir(curFrequency, fileName)
            try:
                os.remove(curFile)
                CronUtils.log.info("Removed cron script %s", curFile)
            except OSError:
                pass

    @staticmethod
    def enableCron(
            name: str,
            script: str,
            frequency: CronFrequency,
            scriptType: str = "!/bin/bash",
    ):
        CronUtils.disableCron(name)
        CronUtils.log.info("Enabling cron %s", name)
        fileName = CronUtils.getFileName(name)
        filePath = CronUtils.getFileDir(frequency, fileName)
        fileContent = """
#""" + scriptType + """
# OQM Cron """ + name + """
# This script placed here by oqm-captain.
""" + script
        with open(filePath, "w") as cronFile:
            cronFile.write(fileContent)

        current_permissions = os.stat(filePath).st_mode
        new_permissions = current_permissions | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH
        os.chmod(filePath, new_permissions)

        CronUtils.log.info("Enabled cron %s at file %s", name, filePath)

    @staticmethod
    def isCronEnabled(name: str) -> bool:
        fileName = CronUtils.getFileName(name)
        for curFrequency in CronFrequency:
            curFile = CronUtils.getFileDir(curFrequency, fileName)
            if os.path.exists(curFile):
                CronUtils.log.debug("Found cron file %s, indicating is enabled.", curFile)
                return True
        return False
