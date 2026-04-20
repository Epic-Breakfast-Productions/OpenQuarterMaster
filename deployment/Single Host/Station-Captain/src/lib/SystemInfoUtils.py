import time
from enum import Enum
from ConfigManager import *
from ServiceUtils import *
from CronUtils import *
from CertsUtils import *
from SnapshotShared import *
from SnapshotBackupUtils import *
import logging
import subprocess
import datetime
import os
import shutil
import tarfile
from LogUtils import *
import psutil
import cpuinfo
import platform

class SystemInfoUtils:
    """

    """
    log = LogUtils.setupLogger("SystemInfoUtils")

    @classmethod
    def getMemSizeB(cls):
        cls.log.info("Getting memory size.")
        return psutil.virtual_memory().total

    @classmethod
    def getMemSizeGB(cls):
        return cls.getMemSizeB() / (1024**3)

    @classmethod
    def getCpuModel(cls):
        return cpuinfo.get_cpu_info().get('brand_raw')

    @classmethod
    def getCpuCount(cls):
        return psutil.cpu_count(logical=True)

    @classmethod
    def getOemId(cls) -> str | None:
        if os.path.isfile("/var/log/installer/oem-id"):
            with open("/var/log/installer/oem-id", 'r') as f:
                return f.read().strip()
        return None

    @classmethod
    def getOsType(cls):
        try:
            systemReleaseInfo = platform.freedesktop_os_release()
            return systemReleaseInfo.get('ID_LIKE', systemReleaseInfo.get('ID', 'unknown'))
        except OSError:
            return 'unknown'

    @classmethod
    def getOsFullName(cls):
        try:
            systemReleaseInfo = platform.freedesktop_os_release()
            return systemReleaseInfo.get('PRETTY_NAME', 'Unknown OS')
        except OSError:
            return 'Unknown OS'

    @classmethod
    def getSystemInfo(cls):
        cls.log.info("Getting system information.")

        # TODO:: registration info

        output = ""
        ipAddrs = subprocess.run(["hostname", "-I"], shell=False, capture_output=True, text=True, check=True).stdout
        ipAddrs = (subprocess.run(["hostname"], shell=False, capture_output=True, text=True,
                                  check=True).stdout.replace("\n", "") + ".local " + ipAddrs)
        ipAddrs = ipAddrs.replace(" ", "\n\t")
        output += "Hostname and IP addresses:\n\t" + ipAddrs
        output += "\tHostname set in configuration:\n\t\t" + mainCM.getConfigVal("system.hostname",
                                                                                 mainCM.configData)
        output += "\n\n\n"

        with open("/etc/os-release") as file:
            osInfo = file.read() + "\n"
        osInfo += subprocess.run(["uname", "-a"], shell=False, capture_output=True, text=True, check=True).stdout
        output += "OS Info:\n" + osInfo + "\n\n\n"

        oemId = cls.getOemId()
        output += "OQM OEM Id:\n" + (oemId if oemId else "N/A") + "\n\n\n"

        hwinfo = subprocess.run(["hwinfo", "--short"], shell=False, capture_output=True, text=True,
                                check=True).stdout
        hwinfo += "\n\nUSB Devices: \n\n"
        hwinfo += subprocess.run(["lsusb"], shell=False, capture_output=True, text=True, check=True).stdout
        hwinfo += "\n\nStorage Devices: \n\n"
        hwinfo += subprocess.run(["lsblk"], shell=False, capture_output=True, text=True, check=True).stdout
        output += "Hardware Info:\n\n" + hwinfo + "\n\n"

        diskInfo = subprocess.run(["df", "-H"], shell=False, capture_output=True, text=True, check=True).stdout
        output += "Disk Usage Info:\n\n" + diskInfo + "\n\n"
        memInfo = subprocess.run(["free", "-h"], shell=False, capture_output=True, text=True, check=True).stdout
        output += "Memory Info:\n\n" + memInfo + "\n\n"
        cls.log.info("Done getting system information.")

        return output



