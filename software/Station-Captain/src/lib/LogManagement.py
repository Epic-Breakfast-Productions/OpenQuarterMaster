import logging
import datetime
import select
import concurrent.futures
import time
import tarfile
from ConfigManager import *
from ServiceUtils import *
import shutil
from systemd import journal


class LogManagement:

    @staticmethod
    def packageServiceLogs(service:str, compilingDir:str):
        outFileName = compilingDir + "/" + service + ".log"
        logging.info("Logging events for %s to file %s", service, outFileName)
        start = time.time()
        with open(outFileName, "w") as outfile:
            result = subprocess.run(["journalctl", "-r", "-u", service], shell=False, text=True, check=False, stdout=outfile)
            if result.returncode != 0:
                logging.error("Failed to get logs for %s: %s", service, result.stderr)
                return False, result.stderr
        logging.info("Finished getting log events in %s seconds for %s", time.time() - start, service)

    @staticmethod
    def packageLogs() -> (bool, str):

        compressionAlg = mainCM.getConfigVal("snapshots.compressionAlg")
        if all(curAlg not in compressionAlg for curAlg in ["xz", "gz", "bz2"]):
            return False, "Configured compression algorithm was invalid."

        snapshotName = "OQM-log-capture-{}".format(datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"))

        logging.debug("Snapshot name: %s", snapshotName)
        compilingDir = ScriptInfo.TMP_DIR + "/logCaptures/" + snapshotName
        snapshotLocation = mainCM.getConfigVal("snapshots.location") + "/logCaptures"
        logArchiveName = "{}/{}.tar.{}".format(snapshotLocation, snapshotName, compressionAlg)

        success = False
        try:
            try:
                os.makedirs(compilingDir)
                os.makedirs(snapshotLocation, exist_ok=True)
            except Exception as e:
                logging.error("Failed to create directories necessary for snapshot taking: %s", e)
                return False, str(e)

            logging.info("Writing log messages.")
            result, services = ServiceUtils.getServiceNames()
            if not result:
                return False, "Failed to get service names: " + services

            executor = concurrent.futures.ProcessPoolExecutor(3)
            futures = [executor.submit(LogManagement.packageServiceLogs, service, compilingDir) for service in services]
            concurrent.futures.wait(futures)
            logging.info("Done writing log messages.")

            logging.info("Archiving log bundle.")
            start = time.time()
            try:
                with tarfile.open(logArchiveName, "x:" + compressionAlg) as tar:
                    tar.add(compilingDir, arcname="")
            except Exception as e:
                logging.error("FAILED to write files to archive: %s", e)
                return False, str(e)
            logging.info("Completed archiving log bundle. Took %s seconds", time.time() - start)
            success = True
            logging.info("Done Performing snapshot.")
            return True, logArchiveName
        finally:
            logging.info("Cleaning up after snapshot operations")

            try:
                if not success:
                    logging.debug("Removing archive file.")
                    if os.path.exists(logArchiveName):
                        os.remove(logArchiveName)
                logging.debug("Removing compiling dir.")
                shutil.rmtree(compilingDir)
                logging.info("Finished cleaning up after snapshot.")
            except Exception as e:
                logging.error("Failed to clean up after performing snapshot operation: %s", e)
