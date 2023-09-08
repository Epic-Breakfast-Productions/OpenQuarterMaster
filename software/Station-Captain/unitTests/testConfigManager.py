import sys
import unittest
from utils import *
import os
import shutil

sys.path.append("../")
from src.lib.ConfigManager import *


class MyTestCase(unittest.TestCase):
    def setUp(self) -> None:
        super().setUp()
        os.makedirs(TEST_CONFIG_ADDENDUM_DIR, exist_ok=True)
        with open(TEST_MAIN_CONFIG, 'x') as stream:
            stream.write('''
{
    "testStr":"config file",
    "testInt":1,
    "testFloat":1.1,
    "overwrittenVal": "old",
    "testObj": {
        "nestedOne": "test"
    }
}
    ''')
        with open(TEST_CONFIG_ADDENDUM_CONFIG_ONE, 'x') as stream:
            stream.write('''
{
    "newVal": "new2" 
}
    ''')
        with open(TEST_CONFIG_ADDENDUM_CONFIG_TWO, 'x') as stream:
            stream.write('''
{
    "overwrittenVal": "new",
    "testObj": {
        "nestedTwo": "test"
    }
}
    ''')
        self.configManager = ConfigManager(
            SecretManager(
                TEST_SECRETS_FILE,
                TEST_SECRET_SECRET_FILE
            ),
            TEST_MAIN_CONFIG,
            TEST_CONFIG_ADDENDUM_DIR
        )

    def tearDown(self) -> None:
        super().tearDown()
        shutil.rmtree(TEST_DATA_DIR)

    def test_configReadFile(self):
        data = ConfigManager.readFile(TEST_MAIN_CONFIG)
        self.assertEqual(
            {
                "testStr": "config file",
                "testInt": 1,
                "testFloat": 1.1,
                "overwrittenVal": "old",
                "testObj": {
                    "nestedOne": "test"
                }
            },
            data
        )

    def test_configRead(self):
        data = self.configManager.configData
        self.assertEqual(
            {
                "testStr": "config file",
                "testInt": 1,
                "testFloat": 1.1,
                "overwrittenVal": "new",
                "newVal": "new2",
                "testObj": {
                    "nestedOne": "test",
                    "nestedTwo": "test"
                }
            },
            data
        )


if __name__ == '__main__':
    unittest.main()
