import datetime
import os.path
import select
import concurrent.futures
import time
import tarfile
from ConfigManager import *
from PackageManagement import *
from ServiceUtils import *
import shutil
from systemd import journal
from LogUtils import *

log = LogUtils.setupLogger(__name__)


class LogManagement:

    @staticmethod
    def packageServiceLogs(service: str, compilingDir: str):
        outFileName = compilingDir + "/10-" + service + ".log"
        log.info("log events for %s to file %s", service, outFileName)
        start = time.time()
        with open(outFileName, "w") as outfile:
            result = subprocess.run(["journalctl", "-r", "-u", service], shell=False, text=True, check=False, stdout=outfile)
            if result.returncode != 0:
                log.error("Failed to get logs for %s: %s", service, result.stderr)
                return False, result.stderr
        log.info("Finished getting log events in %s seconds for %s", time.time() - start, service)

    @staticmethod
    def getSystemInfo() -> str:
        log.info("Getting system information.")
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

        if os.path.isfile("/var/log/installer/oem-id"):
            with open("/var/log/installer/oem-id", 'r') as f:
                output += "OQM OEM Id:\n" + f.read() + "\n\n\n"

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
        log.info("Done getting system information.")

        return output

    @staticmethod
    def packageLogs() -> (bool, str):
        compressionAlg = mainCM.getConfigVal("snapshots.compressionAlg")
        if all(curAlg not in compressionAlg for curAlg in ["xz", "gz", "bz2"]):
            return False, "Configured compression algorithm was invalid."

        logCaptureName = "OQM-log-capture-{}".format(datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"))

        log.debug("Log capture name: %s", logCaptureName)
        compilingDir = ScriptInfo.TMP_DIR + "/logCaptures/" + logCaptureName
        logCaptureLocation = mainCM.getConfigVal("snapshots.location") + "/logCaptures"
        logArchiveName = "{}/{}.tar.{}".format(logCaptureLocation, logCaptureName, compressionAlg)
        otherLogsDir = compilingDir + "/otherLogs"

        success = False
        try:
            try:
                os.makedirs(compilingDir)
                os.makedirs(otherLogsDir)
                os.makedirs(logCaptureLocation, exist_ok=True)
            except Exception as e:
                log.error("Failed to create directories necessary for snapshot taking: %s", e)
                return False, str(e)

            with open(compilingDir + "/00-sysInfo.txt", "w") as sysInfoFile:
                sysInfoFile.write(LogManagement.getSystemInfo())

            with open(compilingDir + "/01-installed.txt", "w") as sysInfoFile:
                sysInfoFile.write(PackageManagement.getOqmPackagesStr(installed=True, notInstalled=False))

            log.info("Writing Service log messages.")
            result, services = ServiceUtils.getServiceNames()
            if not result:
                return False, "Failed to get service names: " + services

            with open(compilingDir + "/02-services.txt", "w") as sysInfoFile:
                if not services:
                    sysInfoFile.write("No Services present. " + services)
                else:
                    sysInfoFile.write("\n".join(services))

            executor = concurrent.futures.ProcessPoolExecutor(3)
            futures = [executor.submit(LogManagement.packageServiceLogs, service, compilingDir) for service in services]
            concurrent.futures.wait(futures)
            log.info("Done writing Service log messages.")

            try:
                shutil.copy(LogUtils.logDir, otherLogsDir)
            except Exception as e:
                log.error("FAILED to copy in other logs: %s", e)
                return False, str(e)

            log.info("Archiving log bundle.")
            start = time.time()
            try:
                with tarfile.open(logArchiveName, "x:" + compressionAlg) as tar:
                    tar.add(compilingDir, arcname="")
            except Exception as e:
                log.error("FAILED to write files to archive: %s", e)
                return False, str(e)
            log.info("Completed archiving log bundle. Took %s seconds. Bundle: %s", time.time() - start, logArchiveName)
            success = True
            log.info("Done Performing snapshot.")
            return True, logArchiveName
        finally:
            log.info("Cleaning up after snapshot operations")

            try:
                if not success:
                    log.debug("Removing archive file.")
                    if os.path.exists(logArchiveName):
                        os.remove(logArchiveName)
                log.debug("Removing compiling dir.")
                shutil.rmtree(compilingDir)
                log.info("Finished cleaning up after snapshot.")
            except Exception as e:
                log.error("Failed to clean up after performing snapshot operation: %s", e)
