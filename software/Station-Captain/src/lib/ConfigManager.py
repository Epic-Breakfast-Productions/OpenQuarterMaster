import base64
import os
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
from cryptography.hazmat.backends import default_backend

CONFIG_MNGR_SECRET_PW_HASH_SALT = b'saltySpittoonHowToughAreYa'
CONFIG_MNGR_SECRET_PW_HASH_ITERATIONS = int(480_000 * 2.5)
CONFIG_MNGR_SECRET_PW_HASH_LEN = 32
CONFIG_MNGR_SECRET_PW_HASH_ALG = hashes.SHA3_512()

CONFIG_MNGR_CONFIGS_DIR = "/etc/oqm/config"
CONFIG_MNGR_MAIN_CONFIG_FILE = CONFIG_MNGR_CONFIGS_DIR + "/mainConfig.json"
CONFIG_MNGR_ADD_CONFIG_DIR = CONFIG_MNGR_CONFIGS_DIR + "/configs"
CONFIG_MNGR_DEFAULT_ADDENDUM_FILE = "99-custom.json"

# https://cryptography.io/en/latest/fernet/#cryptography.fernet.Fernet
class SecretManager:

    @staticmethod
    def testEncrypt():
        origPassword = b"password"

        salt = CONFIG_MNGR_SECRET_PW_HASH_SALT
        kdf = PBKDF2HMAC(
            algorithm=CONFIG_MNGR_SECRET_PW_HASH_ALG,
            length=CONFIG_MNGR_SECRET_PW_HASH_LEN,
            salt=CONFIG_MNGR_SECRET_PW_HASH_SALT,
            iterations=CONFIG_MNGR_SECRET_PW_HASH_ITERATIONS,
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
            algorithm=CONFIG_MNGR_SECRET_PW_HASH_ALG,
            length=CONFIG_MNGR_SECRET_PW_HASH_LEN,
            salt=CONFIG_MNGR_SECRET_PW_HASH_SALT,
            iterations=CONFIG_MNGR_SECRET_PW_HASH_ITERATIONS,
            backend=default_backend()  # not required in newer versions of python lib
        )

        key2 = base64.urlsafe_b64encode(kdf.derive(origPassword))
        f2 = Fernet(key2)

        decMessage = f2.decrypt(encMessage).decode()
        print("decrypted string from f2: ", decMessage)


# TODO:: all this
class ConfigManager:

    def __init__(self):
        self.fernet = Fernet(base64.urlsafe_b64encode(
            PBKDF2HMAC(
                algorithm=CONFIG_MNGR_SECRET_PW_HASH_ALG,
                length=CONFIG_MNGR_SECRET_PW_HASH_LEN,
                salt=CONFIG_MNGR_SECRET_PW_HASH_SALT,
                iterations=CONFIG_MNGR_SECRET_PW_HASH_ITERATIONS,
                backend=default_backend()  # not required in newer versions of python lib
            ).derive(
                ConfigManager.getSecretPassword()
            )
        ))

    @staticmethod
    def getSecretPassword() -> bytes:
        """
        :exception: Exception - when happens
        :return: the ting
        """
        # TODO:: this, securely enough
        return b"password"
