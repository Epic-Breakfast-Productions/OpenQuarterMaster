import logging
import logging.handlers
import os
import shutil
from ConfigManager import *
from ServiceUtils import *
from pathlib import Path


class LogUtils:
    """
    https://docs.python.org/3/library/logging.html

    TODO:: figure out best way to do this, and get log level globally. Might need to not do this quite this way
    """
    logLevel = logging.WARNING
    logDir = "/var/log/oqm/"
    logFile = "non.log"

    @staticmethod
    def setupLogging(logFile:str, console:bool=True):
        Path(LogUtils.logDir).mkdir(parents=True, exist_ok=True)
        LogUtils.logFile = logFile

        if "-vvvv" in sys.argv:
            LogUtils.logLevel = logging.DEBUG

    @staticmethod
    def setupLogger(name: str) -> logging.Logger:
        fh = logging.handlers.RotatingFileHandler(LogUtils.logDir+LogUtils.logFile, maxBytes=10*1024*1024, backupCount=5)
        fh.setLevel(logging.DEBUG)

        # sh = logging.StreamHandler()
        # sh.setLevel(LogUtils.logLevel)

        logOut = logging.getLogger(name)
        logOut.setLevel(LogUtils.logLevel)
        logOut.addHandler(fh)
        # logOut.addHandler(sh)
        return logOut

