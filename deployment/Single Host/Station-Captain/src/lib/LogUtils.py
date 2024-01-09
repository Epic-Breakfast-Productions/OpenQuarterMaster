import logging
import os
import shutil
from ConfigManager import *
from ServiceUtils import *


class LogUtils:
    """
    TODO:: figure out best way to do this, and get log level globally. Might need to not do this quite this way
    """
    logLevel = logging.DEBUG

    @staticmethod
    def setupLogger(name: str):
        logOut = logging.getLogger(name)
        logOut.setLevel(LogUtils.logLevel)

