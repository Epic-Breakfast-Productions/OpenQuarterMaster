import logging
import logging.handlers
import os
import shutil
import sys
from pathlib import Path
from string import Formatter


class LogUtils:
    """
    https://docs.python.org/3/library/logging.html
    """
    fileLogLevel = logging.DEBUG
    consoleLogLevel = logging.DEBUG
    logDir = "/var/log/oqm/"
    logFile = ""
    fileHandler = None
    consoleHandler = None

    @staticmethod
    def setupLogging(logFile:str, console:bool=False):
        """
        Only to be run at beginning of script, before any modules call setupLogger
        :param logFile:
        :param console:
        :return:
        """
        # print("Logging to file: " + logFile)
        logFile = LogUtils.logDir + logFile
        LogUtils.logFile = logFile
        filePresent:bool = False
        if not os.path.exists(logFile):
            try:
                Path(LogUtils.logDir).mkdir(parents=True, exist_ok=True)
                # os.mknod(logFile)
                filePresent = True
            except Exception as e:
                LogUtils.logFile = False
                filePresent = False
        else:
            filePresent = True
        logging.basicConfig(level=logging.WARNING)
        if console:
            fh = logging.StreamHandler(sys.stdout)
            fh.setFormatter(logging.Formatter('%(levelname)-8s :: %(name)-13s :: %(message)s'))
            fh.setLevel(LogUtils.consoleLogLevel)
            LogUtils.consoleHandler = fh

        # logging.basicConfig(level=LogUtils.logLevel)
        if filePresent:
            # print("Logging to file path " + LogUtils.logFile)
            fh = logging.handlers.RotatingFileHandler(LogUtils.logFile, mode="a", maxBytes=10*1024*1024, backupCount=5)
            fh.setFormatter(logging.Formatter('%(asctime)s :: %(levelname)-8s :: %(name)-13s :: %(message)s'))
            fh.setLevel(LogUtils.fileLogLevel)
            LogUtils.fileHandler = fh


    @staticmethod
    def setupLogger(name: str) -> logging.Logger:
        """
        Call to setup a new logger for a module
        :param name:
        :return:
        """
        # sh = logging.StreamHandler()
        # sh.setLevel(LogUtils.logLevel)

        logOut = logging.getLogger(name)
        logOut.propagate = False
        # print(LogUtils.logLevel == logging.DEBUG)

        logOut.setLevel(logging.DEBUG)

        if LogUtils.fileHandler is not None:
            logOut.addHandler(LogUtils.fileHandler)
        if LogUtils.consoleHandler is not None:
            logOut.addHandler(LogUtils.consoleHandler)

        # logOut.addHandler(sh)
        return logOut
