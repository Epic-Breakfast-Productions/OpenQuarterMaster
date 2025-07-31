import sys
import types
import importlib
import unittest

# Provide dummy modules for optional dependencies so OtherUtils can be imported
sys.modules.setdefault("ConfigManager", types.ModuleType("ConfigManager"))
sys.modules.setdefault("ServiceUtils", types.ModuleType("ServiceUtils"))

dummy_logutils = types.ModuleType("LogUtils")

class _DummyLogger:
    def info(self, *args, **kwargs):
        pass
    def error(self, *args, **kwargs):
        pass
    def warning(self, *args, **kwargs):
        pass

class LogUtils:
    @staticmethod
    def setupLogger(name):
        return _DummyLogger()

dummy_logutils.LogUtils = LogUtils
dummy_logutils.setupLogger = LogUtils.setupLogger

sys.modules.setdefault("LogUtils", dummy_logutils)
sys.modules.setdefault("docker", types.ModuleType("docker"))

sys.path.append("../src/lib")
other_utils_module = importlib.import_module("OtherUtils")


class TestOtherUtils(unittest.TestCase):
    def test_human_size_large_value(self):
        """Ensure extremely large sizes do not raise an error"""
        try:
            result = other_utils_module.OtherUtils.human_size(2 ** 100)
        except Exception as exc:
            self.fail(f"human_size raised an exception: {exc}")
        self.assertIsInstance(result, str)
        self.assertTrue(len(result) > 0)


if __name__ == "__main__":
    unittest.main()
