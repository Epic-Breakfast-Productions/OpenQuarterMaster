
TEST_DATA_DIR = "testData"
TEST_CONFIG_ADDENDUM_DIR = TEST_DATA_DIR + "/configs"
TEST_CONFIG_ADDENDUM_CONFIG_ONE = TEST_CONFIG_ADDENDUM_DIR + "/10-test.json"
TEST_CONFIG_ADDENDUM_CONFIG_TWO = TEST_CONFIG_ADDENDUM_DIR + "/11-test.json"
TEST_MAIN_CONFIG = TEST_DATA_DIR + "/main.json"

TEST_SECRETS_FILE = TEST_DATA_DIR + "secrets.json"
TEST_SECRET_SECRET_FILE = TEST_DATA_DIR + "secretSecret.dat"


# TODO:: utility to setup testData


def test_cleanup():
    print("Clean?")