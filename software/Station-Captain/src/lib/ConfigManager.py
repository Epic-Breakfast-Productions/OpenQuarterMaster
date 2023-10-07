import base64
import datetime
import logging
import uuid
from pathlib import Path
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.backends import default_backend
import secrets
import json
from json import JSONDecodeError
import os
import sys
import collections.abc
from ScriptInfos import *

CONFIG_MNGR_MAIN_CONFIG_FILE = ScriptInfo.CONFIG_DIR + "/mainConfig.json"
CONFIG_MNGR_DEFAULT_ADDENDUM_FILE = ScriptInfo.CONFIG_VALUES_DIR + "/99-custom.json"

SECRET_MNGR_SECRET_PW_HASH_SALT = b'saltySpittoonHowToughAreYa'
SECRET_MNGR_SECRET_PW_HASH_ITERATIONS = int(480_000 * 2.5)
SECRET_MNGR_SECRET_PW_HASH_LEN = 32
SECRET_MNGR_SECRET_PW_HASH_ALG = hashes.SHA3_512()
SECRET_MNGR_SECRET_PLACEHOLDER = "<secret>"

SECRET_MNGR_SECRETS_FILE = ScriptInfo.CONFIG_DIR + "/secrets.json"
SECRET_MNGR_SECRETS_SECRET_FILE = ScriptInfo.CONFIG_DIR + "/.secretSecret.dat"


# https://cryptography.io/en/latest/fernet/#cryptography.fernet.Fernet
class SecretManager:
    """
    References:
        - https://www.geeksforgeeks.org/how-to-encrypt-and-decrypt-strings-in-python/#
        - https://cryptography.io/en/latest/fernet/#using-passwords-with-fernet
        - https://docs.python.org/3/library/secrets.html
    """

    def __init__(
            self,
            secretsFile: str = SECRET_MNGR_SECRETS_FILE,
            secretSecretFile: str = SECRET_MNGR_SECRETS_SECRET_FILE,
    ):
        self.secretsFile = secretsFile
        self.secretSecretFile = secretSecretFile
        # ensure secrets file exists
        try:
            os.makedirs(ScriptInfo.CONFIG_VALUES_DIR, exist_ok=True)
            if not os.path.isfile(SECRET_MNGR_SECRETS_FILE):
                with open(SECRET_MNGR_SECRETS_FILE, 'x') as stream:
                    stream.write('''
{
}
        ''')
        except OSError as e:
            # TODO:: just throw error
            print(
                "Error: failed to setup default configuration. Try running as root, at least for first run. Error: ",
                e,
                file=sys.stderr
            )
            exit(1)
        self.fernet = Fernet(base64.urlsafe_b64encode(
            PBKDF2HMAC(
                algorithm=SECRET_MNGR_SECRET_PW_HASH_ALG,
                length=SECRET_MNGR_SECRET_PW_HASH_LEN,
                salt=SECRET_MNGR_SECRET_PW_HASH_SALT,
                iterations=SECRET_MNGR_SECRET_PW_HASH_ITERATIONS,
                backend=default_backend()  # not required in newer versions of python lib
            ).derive(
                self.getSecretPassword()
            )
        ))

    def setSecret(self, key: str, plainVal: str, secretsDict: dict = None):
        if secretsDict is None:
            secretsDict = ConfigManager.readFile(self.secretsFile)

        secretsDict[key] = self.fernet.encrypt(bytes(plainVal, 'utf-8')).decode('utf-8')
        jsonData = json.dumps(secretsDict, indent=4)
        try:
            with open(self.secretsFile, 'w') as stream:
                stream.write(jsonData)
        except OSError as e:
            # TODO:: throw exception
            print("Error: failed to write new secret to secrets file. Error: ",
                  e,
                  file=sys.stderr)
            exit(1)

    def getSecretVal(self, key: str, generateIfNone: bool = True) -> str:
        secretsDict = ConfigManager.readFile(self.secretsFile)
        output = SECRET_MNGR_SECRET_PLACEHOLDER
        if key in secretsDict.keys():
            output = secretsDict[key]
            output = self.fernet.decrypt(bytes(output, 'utf-8')).decode('utf-8')
        elif generateIfNone:
            output = SecretManager.newSecret()
            self.setSecret(key, output, secretsDict)

        return output

    def updateSecrets(self, key: str, val, generateIfNone: bool = True):
        if isinstance(val, str):
            if self.valIsSecret(val):
                val = self.getSecretVal(key, generateIfNone)
        elif isinstance(val, list):
            for i, s in enumerate(val):
                val[i] = self.updateSecrets(
                    key + ".[" + i + "]",
                    s,
                    generateIfNone
                )
        elif isinstance(val, dict):
            for k, v in val.items():
                val[k] = self.updateSecrets(
                    key + "." + k,
                    v,
                    generateIfNone
                )
        return val

    @staticmethod
    def valIsSecret(value: str) -> bool:
        return SECRET_MNGR_SECRET_PLACEHOLDER == value

    @staticmethod
    def newSecret():
        return secrets.token_urlsafe(24)

    def getSecretPassword(self) -> bytes:
        """
        Ideas:

         - File that is readonly for root. Will need to ensure here that that is the case
            - https://stackoverflow.com/questions/1830618/how-to-find-the-owner-of-a-file-or-directory-in-python

        :exception: Exception - when happens
        :return: the ting
        """
        secretFilePath = Path(self.secretSecretFile)
        try:
            if not secretFilePath.exists():
                with open(self.secretSecretFile, 'x') as stream:
                    stream.write(secrets.token_hex(64))
                    stream.write(str(uuid.uuid4()))
                    stream.write(str(datetime.datetime.now()))
                    stream.write(secrets.token_hex(64))
            secretFilePath.chmod(0o400)
        except OSError as e:
            # TODO:: throw exception
            print("Error: failed to write new secret secret file. Error: ",
                  e,
                  file=sys.stderr)
            exit(1)

        with open(self.secretSecretFile, 'r') as stream:
            return bytes(stream.read(), 'utf-8')

    @staticmethod
    def testEncrypt():
        origPassword = b"password"

        salt = SECRET_MNGR_SECRET_PW_HASH_SALT
        kdf = PBKDF2HMAC(
            algorithm=SECRET_MNGR_SECRET_PW_HASH_ALG,
            length=SECRET_MNGR_SECRET_PW_HASH_LEN,
            salt=SECRET_MNGR_SECRET_PW_HASH_SALT,
            iterations=SECRET_MNGR_SECRET_PW_HASH_ITERATIONS,
            backend=default_backend()  # not required in newer versions of python lib
        )

        key1 = base64.urlsafe_b64encode(kdf.derive(origPassword))
        f1 = Fernet(key1)

        message = "hello geeks"

        encMessage = f1.encrypt(message.encode())
        encMessageStr = encMessage.decode('utf-8')

        print("key: ", key1)
        print("original string: ", message)
        print("encrypted string (bytes): ", encMessage)
        print("encrypted string: ", encMessageStr)

        decMessage = f1.decrypt(encMessage).decode()
        print("decrypted string from f1: ", decMessage)

        kdf = PBKDF2HMAC(
            algorithm=SECRET_MNGR_SECRET_PW_HASH_ALG,
            length=SECRET_MNGR_SECRET_PW_HASH_LEN,
            salt=SECRET_MNGR_SECRET_PW_HASH_SALT,
            iterations=SECRET_MNGR_SECRET_PW_HASH_ITERATIONS,
            backend=default_backend()  # not required in newer versions of python lib
        )

        key2 = base64.urlsafe_b64encode(kdf.derive(origPassword))
        f2 = Fernet(key2)

        decMessage = f2.decrypt(encMessage).decode()
        print("decrypted string from f2: ", decMessage)
        print("new secret: ", SecretManager.newSecret())

        decMessage = f2.decrypt(bytes(encMessageStr, 'utf-8')).decode('utf-8')
        print("decrypted string from string: ", decMessage)


# Exception to throw when config errors occur
class ConfigKeyNotFoundException(Exception):
    pass


class ConfigManager:
    configData = {}

    def __init__(
            self,
            secretManager: SecretManager = None,
            mainConfigFile: str = CONFIG_MNGR_MAIN_CONFIG_FILE,
            additionalConfigsDir: str = ScriptInfo.CONFIG_VALUES_DIR
    ):
        self.secretManager = secretManager
        self.additionalConfigsDir = additionalConfigsDir
        self.mainConfigFile = mainConfigFile
        # Ensure main config, additional configs dir exist
        try:
            os.makedirs(self.additionalConfigsDir, exist_ok=True)
            if not os.path.isfile(self.mainConfigFile):
                with open(self.mainConfigFile, 'x') as stream:
                    # TODO:: move this default template to another file?
                    stream.write('''
{
    "system": {
    },
    "captain": {
    },
    "snapshots": {
        "location": "/data/oqm-snapshots/",
        "numToKeep": 5,
        "frequency": "weekly"
    }
}
        ''')
        except OSError as e:
            # TODO:: just throw error
            print(
                "Error: failed to setup default configuration. Try running as root, at least for first run. Error: ",
                e,
                file=sys.stderr
            )
            exit(1)
        # Read in configuration
        self.configData = self.readFile(self.mainConfigFile)
        for file in os.listdir(self.additionalConfigsDir):
            if file.endswith(".json"):
                curUpdates = self.readFile(self.additionalConfigsDir + "/" + file)
                self.configData = ConfigManager.mergeDicts(self.configData, curUpdates)
            else:
                continue

    def getSecretManager(self) -> SecretManager:
        if self.secretManager is None:
            # print("DEBUG:: getting new secret manager")
            self.secretManager = SecretManager()
        return self.secretManager

    def getConfigValRec(self, configKeyOrig: str, configKey: str, data: dict, formatData) -> str:
        """
        Recursive function to get a particular value in the data
        :param configKeyOrig: The configuration to get, dot notation I.E. "test.value"
        :param configKey: At first call, same as configKeyOrig. Is whittled down until reaching final segment in dot notation.
        :param data: The dict to find the configuration in
        :param formatData: If returning an object or list, if to format the data 'pretty'
        :except ConfigKeyNotFoundException if a value could not be found for the given key
        :return: The value found as a String
        """
        # print("debug:: configKey: " + configKey)
        # print("debug:: data: " + json.dumps(data, indent=4))
        if not isinstance(data, dict):
            raise ConfigKeyNotFoundException()
        if "." in configKey:
            parts = configKey.split(".", 1)
            curConfig = parts[0]
            keyLeft = parts[1]

            if curConfig not in data:
                raise ConfigKeyNotFoundException()

            return self.getConfigValRec(configKeyOrig, keyLeft, data[curConfig], formatData)
        if configKey not in data:
            raise ConfigKeyNotFoundException()
        result = data[configKey]
        if isinstance(result, (dict, list, str)):
            result = self.getSecretManager().updateSecrets(configKeyOrig, result)
            if not isinstance(result, str):
                if formatData:
                    result = json.dumps(result, indent=4)
                else:
                    result = json.dumps(result)
        else:
            result = str(result)
        return result

    def getConfigVal(self, configKey: str, data: dict, formatData=True) -> str:
        return self.getConfigValRec(configKey, configKey, data, formatData)

    @staticmethod
    def getArrRef(configKey: str):
        logging.debug('todo')

    @staticmethod
    def setConfigVal(configKey: str, configVal: str, data: dict):
        """
        Updates the dict in place. Recursive
        :param configKey: The key in dot notation of the value to set
        :param configVal: The value to set
        :param data: The dict to update
        """
        if not isinstance(data, dict):
            raise ConfigKeyNotFoundException()

        if "." not in configKey and "[" not in configKey:
            data[configKey] = configVal
            return

        if "." in configKey:
            parts = configKey.split(".", 1)
            curConfig = parts[0]
            keyLeft = parts[1]

            # TODO:: add array stuff here
            # if "[" in keyLeft:
            #
            # else:
            if curConfig not in data:
                data[curConfig] = {}
            ConfigManager.setConfigVal(keyLeft, configVal, data[curConfig])
        else:
            # TODO:: add array stuff here
            logging.warn("err")

    @staticmethod
    def setConfigValInFile(
            configKeyToSet: str,
            configValToSet: str,
            configFile: str = CONFIG_MNGR_DEFAULT_ADDENDUM_FILE,
            mainConfigFile: str = CONFIG_MNGR_MAIN_CONFIG_FILE,
            additionalConfigDir: str = ScriptInfo.CONFIG_VALUES_DIR,
            defaultAddendumFile: str = CONFIG_MNGR_DEFAULT_ADDENDUM_FILE,
    ) -> str:
        if configFile == ".":
            configFile = mainConfigFile
        elif configFile == "":
            configFile = defaultAddendumFile
        else:
            configFile = additionalConfigDir + "/" + configFile

        try:
            if not os.path.isfile(configFile):
                with open(configFile, 'x') as stream:
                    stream.write('''
{
}
    ''')
        except OSError as e:
            # TODO:: throw exception
            print(
                "Error: failed to set configuration value; can't create new config file. Try running as root. Error: ",
                e,
                file=sys.stderr)
            exit(1)

        curVals = ConfigManager.readFile(configFile)

        try:
            ConfigManager.setConfigVal(configKeyToSet, configValToSet, curVals)
        except ConfigKeyNotFoundException as e:
            # TODO:: throw exception
            print("Error: Bad config key given. Possibly trying to overwrite an existing value with a new object.",
                  file=sys.stderr)
            exit(1)

        jsonData = json.dumps(curVals, indent=4)
        try:
            with open(configFile, 'w') as stream:
                stream.write(jsonData)
        except OSError as e:
            # TODO:: throw exception
            print("Error: failed to set configuration value; can't write to config file. Try running as root. Error: ",
                  e,
                  file=sys.stderr)
            exit(1)

        return jsonData

    def setSecretValInFile(
            self,
            configKeyToSet: str,
            configValToSet: str,
            configFile: str = CONFIG_MNGR_DEFAULT_ADDENDUM_FILE,
            mainConfigFile: str = CONFIG_MNGR_MAIN_CONFIG_FILE,
            additionalConfigDir: str = ScriptInfo.CONFIG_VALUES_DIR,
            defaultAddendumFile: str = CONFIG_MNGR_DEFAULT_ADDENDUM_FILE,
    ) -> str:
        self.getSecretManager().setSecret(configKeyToSet, configValToSet)
        output = ConfigManager.setConfigValInFile(
            configKeyToSet, SECRET_MNGR_SECRET_PLACEHOLDER, configFile, mainConfigFile, additionalConfigDir,
            defaultAddendumFile
        )
        return output

    @staticmethod
    def readFile(file: str) -> dict:
        """
        Reads a file into a python dict. If error in reading or parsing, exits
        :param file:
        :return:
        """
        try:
            with open(file, 'r') as stream:
                return json.load(stream)
        except (OSError, JSONDecodeError) as e:
            # TODO:: just throw error
            print("Error: failed to read file " + file + " into configuration: ", e, file=sys.stderr)
            exit(1)

    @staticmethod
    def mergeDicts(d, u):
        """
        https://stackoverflow.com/a/3233356/3015723
        :param d:
        :param u:
        :return:
        """
        for k, v in u.items():
            if isinstance(v, collections.abc.Mapping):
                d[k] = ConfigManager.mergeDicts(d.get(k, {}), v)
            else:
                d[k] = v
        return d


mainCM = ConfigManager()
