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

    @classmethod
    def getScriptContent(cls, name: str, script: str, scriptType:str) -> str:
        output = ""

        if scriptType == "bash":
            output += """#!/bin/bash
            # OQM Cron """ + name + """
            # This script placed here by oqm-captain.
            logfile=/var/log/oqm/cron-"""+name+""".log
            
            echo "$(date) - """ + name + """ starting." >> $logfile
            
            ((
                """ + script + """
            ) 2>&1) | tee -a "$logfile"
            
            echo "$(date) - """ + name + """ Finished." >> $logfile
            """
        else:
            raise ValueError("Script type not supported: " + scriptType)

        return output

    @classmethod
    def enableCron(
            cls,
            name: str,
            script: str,
            frequency: CronFrequency,
            scriptType: str = "bash",
    ):
        CronUtils.disableCron(name)
        CronUtils.log.info("Enabling cron %s", name)
        fileName = CronUtils.getFileName(name)
        filePath = CronUtils.getFileDir(frequency, fileName)
        fileContent = cls.getScriptContent(name, script, scriptType)
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
