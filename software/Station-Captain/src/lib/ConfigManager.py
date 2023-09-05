import base64
import os
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.backends import default_backend
import secrets
import json
from json import JSONDecodeError
import os
import sys

SECRET_MNGR_SECRET_PW_HASH_SALT = b'saltySpittoonHowToughAreYa'
SECRET_MNGR_SECRET_PW_HASH_ITERATIONS = int(480_000 * 2.5)
SECRET_MNGR_SECRET_PW_HASH_LEN = 32
SECRET_MNGR_SECRET_PW_HASH_ALG = hashes.SHA3_512()
SECRET_MNGR_SECRET_PLACEHOLDER = "<secret>"

CONFIG_MNGR_CONFIGS_DIR = "/etc/oqm/config"
CONFIG_MNGR_MAIN_CONFIG_FILE = CONFIG_MNGR_CONFIGS_DIR + "/mainConfig.json"
CONFIG_MNGR_ADD_CONFIG_DIR = CONFIG_MNGR_CONFIGS_DIR + "/configs"
CONFIG_MNGR_DEFAULT_ADDENDUM_FILE = "99-custom.json"


# https://cryptography.io/en/latest/fernet/#cryptography.fernet.Fernet
class SecretManager:

    def __init__(self):
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

    def getSecretVal(self, key: str, generateIfNone: bool = True) -> str:
        # TODO
        return key

    @staticmethod
    def valIsSecret(value: str) -> bool:
        return SECRET_MNGR_SECRET_PLACEHOLDER == value

    @staticmethod
    def newSecret():
        return secrets.token_urlsafe(24)

    @staticmethod
    def getSecretPassword() -> bytes:
        """
        :exception: Exception - when happens
        :return: the ting
        """
        # TODO:: this, securely enough
        return b"password"

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

        print("key: ", key1)
        print("original string: ", message)
        print("encrypted string: ", encMessage)

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


# Exception to throw when config errors occur
class ConfigKeyNotFoundException(Exception):
    pass


# TODO:: all this
class ConfigManager:
    secretManager = SecretManager()
    configData = {}

    def __init__(self):
        # Ensure main config, additional configs dir exist
        try:
            os.makedirs(CONFIG_MNGR_ADD_CONFIG_DIR, exist_ok=True)
            if not os.path.isfile(CONFIG_MNGR_MAIN_CONFIG_FILE):
                with open(CONFIG_MNGR_MAIN_CONFIG_FILE, 'x') as stream:
                    stream.write('''
{
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
        self.configData = self.readFile(CONFIG_MNGR_MAIN_CONFIG_FILE)
        for file in os.listdir(CONFIG_MNGR_ADD_CONFIG_DIR):
            if file.endswith(".json"):
                curUpdates = self.readFile(CONFIG_MNGR_ADD_CONFIG_DIR + "/" + file)
                self.configData.update(curUpdates)
            else:
                continue

    @staticmethod
    def getConfigValRec(configKey: str, data: dict, formatData) -> str:
        """
        Recursive function to get a particular value in the data
        :param configKey: The configuration to get, dot notation I.E. "test.value"
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

            return ConfigManager.getConfigValRec(keyLeft, data[curConfig], formatData)
        if configKey not in data:
            raise ConfigKeyNotFoundException()
        result = data[configKey]
        if isinstance(result, (dict, list)):
            if formatData:
                result = json.dumps(result, indent=4)
            else:
                result = json.dumps(result)
        elif not isinstance(result, str):
            result = str(result)
            # TODO:: secret dealings
        return result

    def getConfigVal(self, configKey: str, data: dict, formatData=True) -> str:
        try:
            return self.getConfigValRec(configKey, data, formatData)
        except ConfigKeyNotFoundException:
            # TODO:: throw exception
            print("ERROR: Config key not found: " + configKey, file=sys.stderr)
            exit(1)

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
        if "." in configKey:
            parts = configKey.split(".", 1)
            curConfig = parts[0]
            keyLeft = parts[1]

            if curConfig not in data:
                data[curConfig] = {}
            ConfigManager.setConfigVal(keyLeft, configVal, data[curConfig])
        else:
            # print("Debug: key: " + configKey)
            # print("Debug: val: " + configVal)
            # print("Debug: data: " + data)
            data[configKey] = configVal

    @staticmethod
    def setConfigValInFile(configKeyToSet: str, configValToSet: str,
                           configFile: str = CONFIG_MNGR_DEFAULT_ADDENDUM_FILE) -> str:
        if configFile == ".":
            configFile = CONFIG_MNGR_MAIN_CONFIG_FILE
        elif configFile != CONFIG_MNGR_DEFAULT_ADDENDUM_FILE:
            configFile = CONFIG_MNGR_ADD_CONFIG_DIR + "/" + configFile

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
