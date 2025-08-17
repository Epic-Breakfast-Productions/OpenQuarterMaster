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
from SystemInfoUtils import *
from SystemInfoUtils import *


class LogManagement:
    log = LogUtils.setupLogger("LogManagement")

    @classmethod
    def setupArgParser(cls, subparsers):
        logs_parser = subparsers.add_parser("package-logs", help="Packages service logs for debugging.")
        logs_parser.set_defaults(func=LogManagement.packageLogsFromArgs)

    @classmethod
    def packageLogsFromArgs(cls, args):
        result, message = cls.packageLogs()
        if not result:
            print("Failed to package logs: " + message)
            exit(3)
        print(message)

    @staticmethod
    def packageServiceLogs(service: str, compilingDir: str):
        outFileName = compilingDir + "/10-" + service + ".log"
        LogManagement.log.info("log events for %s to file %s", service, outFileName)
        start = time.time()
        with open(outFileName, "w") as outfile:
            result = subprocess.run(["journalctl", "-r", "-u", service], shell=False, text=True, check=False, stdout=outfile)
            if result.returncode != 0:
                LogManagement.log.error("Failed to get logs for %s: %s", service, result.stderr)
                return False, result.stderr
        LogManagement.log.info("Finished getting log events in %s seconds for %s", time.time() - start, service)

    @staticmethod
    def packageLogs() -> (bool, str):
        compressionAlg = mainCM.getConfigVal("snapshots.compressionAlg")
        if all(curAlg not in compressionAlg for curAlg in ["xz", "gz", "bz2"]):
            return False, "Configured compression algorithm was invalid."

        logCaptureName = "OQM-log-capture-{}".format(datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"))

        LogManagement.log.debug("Log capture name: %s", logCaptureName)
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
                LogManagement.log.error("Failed to create directories necessary for snapshot taking: %s", e)
                return False, str(e)

            with open(compilingDir + "/00-sysInfo.txt", "w") as sysInfoFile:
                sysInfoFile.write(SystemInfoUtils.getSystemInfo())

            with open(compilingDir + "/01-installed.txt", "w") as sysInfoFile:
                sysInfoFile.write(PackageManagement.getOqmPackagesStr(installed=True, notInstalled=False))

            LogManagement.log.info("Writing Service log messages.")
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
            LogManagement.log.info("Done writing Service log messages.")

            try:
                shutil.copy(LogUtils.logDir, otherLogsDir)
            except Exception as e:
                LogManagement.log.error("FAILED to copy in other logs: %s", e)
                return False, str(e)

            LogManagement.log.info("Archiving log bundle.")
            start = time.time()
            try:
                with tarfile.open(logArchiveName, "x:" + compressionAlg) as tar:
                    tar.add(compilingDir, arcname="")
            except Exception as e:
                LogManagement.log.error("FAILED to write files to archive: %s", e)
                return False, str(e)
            LogManagement.log.info("Completed archiving log bundle. Took %s seconds. Bundle: %s", time.time() - start, logArchiveName)
            success = True
            LogManagement.log.info("Done Performing snapshot.")
            return True, logArchiveName
        finally:
            LogManagement.log.info("Cleaning up after snapshot operations")

            try:
                if not success:
                    LogManagement.log.debug("Removing archive file.")
                    if os.path.exists(logArchiveName):
                        os.remove(logArchiveName)
                LogManagement.log.debug("Removing compiling dir.")
                shutil.rmtree(compilingDir)
                LogManagement.log.info("Finished cleaning up after snapshot.")
            except Exception as e:
                LogManagement.log.error("Failed to clean up after performing snapshot operation: %s", e)
