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
        filePresent:bool = False
        if not os.path.exists(logFile):
            try:
                Path(LogUtils.logDir).mkdir(parents=True, exist_ok=True)
                os.mknod(logFile)
                LogUtils.logFile = logFile
                filePresent = True
            except Exception as e:
                LogUtils.logFile = False
                filePresent = False
        else:
            filePresent = True
        logging.basicConfig(level=logging.NOTSET)
        if console:
            LogUtils.logLevel = logging.DEBUG

    @staticmethod
    def setupLogger(name: str) -> logging.Logger:
        # sh = logging.StreamHandler()
        # sh.setLevel(LogUtils.logLevel)

        logOut = logging.getLogger(name)
        # print(LogUtils.logLevel == logging.DEBUG)
        logOut.setLevel(LogUtils.logLevel)

        if not LogUtils.logFile:
            fh = logging.handlers.RotatingFileHandler(LogUtils.logDir+LogUtils.logFile, maxBytes=10*1024*1024, backupCount=5)
            fh.setLevel(logging.DEBUG)
            logOut.addHandler(fh)

        # logOut.addHandler(sh)
        return logOut

