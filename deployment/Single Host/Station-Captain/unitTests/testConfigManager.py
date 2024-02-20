import socket
import sys
import unittest
from utils import *
import os
import shutil

sys.path.append("../src/lib")
from ConfigManager import *


class MyTestCase(unittest.TestCase):
    def setUp(self) -> None:
        super().setUp()
        os.makedirs(TEST_CONFIG_ADDENDUM_DIR, exist_ok=True)
        with open(TEST_MAIN_CONFIG, 'x') as stream:
            stream.write('''
{
    "testStr":"config file",
    "testSecret":"<secret>",
    "testInt":1,
    "testFloat":1.1,
    "overwrittenVal": "old",
    "testObj": {
        "nestedOne": "test"
    },
    "testArr" : ["1", "2", "3"],
    "testReplacement" : "#{testStr} - #{testInt} - #{testFloat} - #{testObj.nestedOne} - #{#mdnsHost}"
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
                "testSecret": "<secret>",
                "testInt": 1,
                "testFloat": 1.1,
                "overwrittenVal": "old",
                "testObj": {
                    "nestedOne": "test"
                },
                "testArr": ["1", "2", "3"],
                "testReplacement": "#{testStr} - #{testInt} - #{testFloat} - #{testObj.nestedOne} - #{#mdnsHost}"
            },
            data
        )

    def test_configRead(self):
        data = self.configManager.configData
        self.assertEqual(
            {
                "testStr": "config file",
                "testSecret": "<secret>",
                "testInt": 1,
                "testFloat": 1.1,
                "overwrittenVal": "new",
                "newVal": "new2",
                "testObj": {
                    "nestedOne": "test",
                    "nestedTwo": "test"
                },
                "testArr": ["1", "2", "3"],
                "testReplacement": "#{testStr} - #{testInt} - #{testFloat} - #{testObj.nestedOne} - #{#mdnsHost}"
            },
            data
        )

    def test_getStr(self):
        data = self.configManager.getConfigVal("testStr")
        self.assertEqual(type(data), str)
        self.assertEqual(data, "config file")

    def test_getInt(self):
        data = self.configManager.getConfigVal("testInt")
        self.assertEqual(type(data), int)
        self.assertEqual(data, 1)

    def test_getFloat(self):
        data = self.configManager.getConfigVal("testFloat")
        self.assertEqual(type(data), float)
        self.assertEqual(data, 1.1)

    def test_getDict(self):
        data = self.configManager.getConfigVal("testObj")
        self.assertEqual(type(data), dict)
        self.assertEqual(data, {
            "nestedOne": "test",
            "nestedTwo": "test"
        })

    def test_getArray(self):
        data = self.configManager.getConfigVal("testArr")
        self.assertEqual(type(data), list)
        self.assertEqual(data, ["1", "2", "3"])

    def test_getSecret(self):
        data = self.configManager.getConfigVal("testSecret")
        self.assertEqual(type(data), str)
        self.assertNotEqual(len(data), 0)
        self.assertNotEqual("<secret>", data)

    def test_getPlaceholder(self):
        data = self.configManager.getConfigVal("testReplacement")
        self.assertEqual(type(data), str)
        self.assertEqual("config file - 1 - 1.1 - test - " + socket.gethostname() + ".local", data)

    def test_configSetSimple(self):
        data = {}
        ConfigManager.setConfigVal("simple", "val", data)
        self.assertEqual(
            {
                "simple": "val"
            },
            data
        )

    def test_configSetDeeper(self):
        data = {}
        ConfigManager.setConfigVal("simple.sub", "val", data)
        self.assertEqual(
            {
                "simple": {
                    "sub": "val"
                }
            },
            data
        )

    def test_configSetSimpleInt(self):
        data = {}
        ConfigManager.setConfigVal("simple", 1, data)
        self.assertEqual(
            {
                "simple": 1
            },
            data
        )

    def test_configSetSimpleFloat(self):
        data = {}
        ConfigManager.setConfigVal("simple", 1.1, data)
        self.assertEqual(
            {
                "simple": 1.1
            },
            data
        )

    def test_configSetSimpleBool(self):
        data = {}
        ConfigManager.setConfigVal("simple", True, data)
        self.assertEqual(
            {
                "simple": True
            },
            data
        )

    def test_newSecretGet(self):
        newSecret = SecretManager.newSecret()
        print("New secret: "+ newSecret)
        # this likes to sometimes be 24?
        self.assertEqual(32, len(newSecret))

    # TODO:: test get arrays

    # def test_configSetSimpleArrayNew(self):
    #     data = {}
    #     ConfigManager.setConfigVal("simple[]", "val", data)
    #     self.assertEqual(
    #         {
    #             "simple": ["val"]
    #         },
    #         data
    #     )
    #
    # def test_configSetSimpleArrayPushExisting(self):
    #     data = {"simple": ["val"]}
    #     ConfigManager.setConfigVal("simple[]", "val2", data)
    #     self.assertEqual(
    #         {
    #             "simple": ["val", "val2"]
    #         },
    #         data
    #     )
    #
    # def test_configSetSimpleArrayReplaceAllExistingWithEmpty(self):
    #     data = {"simple": ["val"]}
    #     ConfigManager.setConfigVal("simple[-]", "", data)
    #     self.assertEqual(
    #         {
    #             "simple": ["val2", "val3", "val4"]
    #         },
    #         data
    #     )
    #
    # def test_configSetSimpleArrayReplaceAllExisting(self):
    #     data = {"simple": ["val"]}
    #     ConfigManager.setConfigVal("simple[-]", "val2,val3,val4", data)
    #     self.assertEqual(
    #         {
    #             "simple": ["val2", "val3", "val4"]
    #         },
    #         data
    #     )
    #
    # def test_configSetSimpleArrayReplaceExisting(self):
    #     data = {"simple": ["val"]}
    #     ConfigManager.setConfigVal("simple[0]", "val2", data)
    #     self.assertEqual(
    #         {
    #             "simple": ["val2"]
    #         },
    #         data
    #     )

    # TODO:: finish tests


if __name__ == '__main__':
    unittest.main()
