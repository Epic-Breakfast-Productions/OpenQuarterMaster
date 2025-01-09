import logging
import logging.handlers
import os
import sys
from pathlib import Path


class LogUtils:
    """
    Facilitates logging in this multi-class/module system.

    Essentially disables the default logging, defers to logger gotten from setupLogger

    Example:

    LogUtils.setupLogging("station-captain.log", "--verbose" in sys.argv) # only call once per running instance
    log = LogUtils.setupLogger("main")

    https://docs.python.org/3/library/logging.html
    """
    defaultFormat =                        '%(levelname)-8s :: %(name)-13s :: %(message)s'
    defaultFormatWithTime = '%(asctime)s :: %(levelname)-8s :: %(name)-13s :: %(message)s'
    fileLogLevel = logging.DEBUG
    consoleLogLevel = logging.DEBUG
    logDir = "/var/log/oqm/"
    logFile = ""
    fileHandler = None
    consoleHandler = None
    logFileMaxSize = 50*1024*1024 # 50 MB
    logFileBackups = 5

    @staticmethod
    def setupLogging(logFile:str, console:bool=False):
        """
        Only to be run at beginning of script (main), before any modules call setupLogger. Only call once.
        :param logFile: The file within LogUtils.logDir to use for this run
        :param console: If to log to the console
        :return: Nothing
        """
        logFile = LogUtils.logDir + logFile
        LogUtils.logFile = logFile
        filePresent:bool = False
        if not os.path.exists(logFile):
            try:
                Path(LogUtils.logDir).mkdir(parents=True, exist_ok=True)
                filePresent = True
            except Exception as e:
                LogUtils.logFile = False
                filePresent = False
        else:
            filePresent = True
        logging.basicConfig(level=logging.WARNING)
        if console:
            fh = logging.StreamHandler(sys.stdout)
            fh.setFormatter(logging.Formatter(LogUtils.defaultFormat))
            fh.setLevel(LogUtils.consoleLogLevel)
            LogUtils.consoleHandler = fh

        if filePresent:
            fh = logging.handlers.RotatingFileHandler(LogUtils.logFile, mode="a", maxBytes=LogUtils.logFileMaxSize, backupCount=LogUtils.logFileBackups)
            fh.setFormatter(logging.Formatter(LogUtils.defaultFormatWithTime))
            fh.setLevel(LogUtils.fileLogLevel)
            LogUtils.fileHandler = fh

    @staticmethod
    def setupLogger(name: str) -> logging.Logger:
        """
        Call to setup a new logger for a module/class/whatever
        :param name: The name to give to the logger
        :return: The logger to use
        """
        logOut = logging.getLogger(name)
        logOut.propagate = False
        logOut.setLevel(logging.DEBUG)

        if LogUtils.fileHandler is not None:
            logOut.addHandler(LogUtils.fileHandler)
        if LogUtils.consoleHandler is not None:
            logOut.addHandler(LogUtils.consoleHandler)

        return logOut
