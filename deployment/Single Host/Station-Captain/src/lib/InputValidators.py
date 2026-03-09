from typing import Optional

from dialog import Dialog
from ConfigManager import *
from ContainerUtils import *
from ServiceUtils import *
from EmailUtils import *
from DataUtils import *
from PackageManagement import *
from CronUtils import *
from SnapshotUtils import *
from LogManagement import *
import time
import re
import os
from LogUtils import *


class InputValidators:
    log = LogUtils.setupLogger("InputValidators")

    @staticmethod
    def getValidatorFor(valString: str):
        if valString == "email":
            return InputValidators.isEmail
        if valString == "notEmpty":
            return InputValidators.isNotEmpty
        if valString == "digit":
            return InputValidators.isDigit
        if valString == "cronKeyword":
            return InputValidators.isCronKeyword
        if valString == "writableDirectory":
            return InputValidators.isWritableDirectory
        return None

    @staticmethod
    def isEmail(val: str) -> Optional[str]:
        # I know this looks dumb, but fullmatch returns an obj and not a straight bool so this makes the linter happy
        if re.fullmatch(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,7}\b', val):
            return None
        return "Was not an email."

    @staticmethod
    def isNotEmpty(val: str) -> Optional[str]:
        if not bool(val and val.strip()):
            return "Value given was blank"
        return None

    @staticmethod
    def isDigit(val: str) -> Optional[str]:
        if not val.isdigit():
            return "Value given was not a number"
        return None

    @staticmethod
    def isCronKeyword(val: str) -> Optional[str]:
        try:
            CronFrequency[val]
        except ValueError:
            return "Value given was not a valid option (" + CronFrequency.getFreqListStr() + ")"
        return None

    @staticmethod
    def isCertMode(val: str) -> Optional[str]:
        if val not in ["self", "letsEncrypt", "provided"]:
            return "Value given was not a valid option (" + CronFrequency.getFreqListStr() + ")"
        return None

    @staticmethod
    def isWritableDirectory(val: str) -> Optional[str]:
        if not os.path.exists(val):
            return "Path given does not exist"
        if not os.path.isdir(val):
            return "Path given is not a directory"
        return None

    @classmethod
    def isValidHostname(cls, val: str) -> Optional[str]:
        cls.log.info("Validating hostname: '" + val + "'")
        if val is "#{#mdnsHost}":
            cls.log.info("Valid - default value")
            return None
        if cls.isNotEmpty(val) is not None:
            cls.log.info("Invalid - blank")
            return "Hostname cannot be blank"
        if any(c.isupper() for c in val):
            cls.log.info("Invalid - contained uppercase characters")
            return "Hostname cannot contain uppercase characters"
        if re.search(r'\s', val) is not None:
            cls.log.info("Invalid - contained whitespace")
            return "Hostname cannot contain whitespace"
        cls.log.info("Valid")
        return None
