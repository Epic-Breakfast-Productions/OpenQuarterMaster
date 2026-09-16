import \
	os


def pytest_configure(config):
	os.environ["CHARACTERISTICS_FILE"] = "./test/characteristics.yaml"
	os.environ["UIS_DATA_DIR"] = "./test/uis/"