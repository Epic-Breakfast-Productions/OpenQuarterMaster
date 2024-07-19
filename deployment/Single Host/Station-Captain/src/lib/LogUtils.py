import logging
import logging.handlers
import os
import shutil
import sys
from pathlib import Path


class LogUtils:
    """
    https://docs.python.org/3/library/logging.html
    """
    logLevel = logging.WARNING
    logDir = "/var/log/oqm/"
    logFile = "non.log"

    @staticmethod
    def setupLogging(logFile:str, console:bool=False):
        Path(LogUtils.logDir).mkdir(parents=True, exist_ok=True)
        LogUtils.logFile = logFile
        logging.basicConfig(level=logging.NOTSET)

        if console:
            LogUtils.logLevel = logging.DEBUG

    @staticmethod
    def setupLogger(name: str) -> logging.Logger:
        fh = logging.handlers.RotatingFileHandler(LogUtils.logDir+LogUtils.logFile, maxBytes=10*1024*1024, backupCount=5)
        fh.setLevel(logging.DEBUG)

        # sh = logging.StreamHandler()
        # sh.setLevel(LogUtils.logLevel)

        logOut = logging.getLogger(name)
        # print(LogUtils.logLevel == logging.DEBUG)
        logOut.setLevel(LogUtils.logLevel)
        logOut.addHandler(fh)
        # logOut.addHandler(sh)
        return logOut

