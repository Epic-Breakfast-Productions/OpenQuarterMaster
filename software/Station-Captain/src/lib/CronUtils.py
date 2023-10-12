import logging
import os
from enum import Enum


class CronFrequency(Enum):
    hourly = 1
    daily = 2
    weekly = 3
    monthly = 4


class CronUtils:

    @staticmethod
    def getFileName(name: str):
        return "oqm-" + name

    @staticmethod
    def getFileDir(frequency: CronFrequency, fileName: str = None):
        output = "/etc/cron.%s".format(frequency.name)

        if fileName is not None:
            output += "/" + fileName
        return output

    @staticmethod
    def disableCron(name: str):
        logging.info("Disabling cron " + name)
        fileName = CronUtils.getFileName(name)

        for curFrequency in CronFrequency:
            curFile = CronUtils.getFileDir(curFrequency, fileName)
            try:
                os.remove(curFile)
                logging.info("Removed cron script %s", curFile)
            except OSError:
                pass

    @staticmethod
    def enableCron(
            name: str,
            script: str,
            frequency: CronFrequency
    ):
        CronUtils.disableCron(name)
        logging.info("Enabling cron %s", name)
        fileName = CronUtils.getFileName(name)
        fileContent = """
#!/bin/bash
# Cron """ + name + """
# This script placed here by oqm-captain.
""" + script
        with open(fileName, "w") as cronFile:
            cronFile.write(fileContent)
        logging.info("Enabled cron %s at file %s", name, fileName)
