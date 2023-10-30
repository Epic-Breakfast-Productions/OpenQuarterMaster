import time
from enum import Enum
from ConfigManager import *
from ServiceUtils import *
from CronUtils import *
import logging
import subprocess
import datetime
import os
import shutil
import tarfile


class SnapshotTrigger(Enum):
    manual = 1
    scheduled = 2


class SnapshotUtils:
    """

    """
    CRON_NAME = "take-snapshot"

    @staticmethod
    def performSnapshot(snapshotTrigger: SnapshotTrigger) -> (bool, str):
        logging.info("Performing snapshot.")

        compressionAlg = mainCM.getConfigVal("snapshots.compressionAlg")

        if all(curAlg not in compressionAlg for curAlg in ["xz", "gz", "bz2"]):
            return False, "Configured compression algorithm was invalid."

        snapshotName = "OQM-snapshot-{}-{}".format(
            datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"),
            snapshotTrigger.name
        )
        logging.debug("Snapshot name: %s", snapshotName)
        compilingDir = ScriptInfo.TMP_DIR + "/snapshots/" + snapshotName
        compilingConfigsDir = os.path.join(compilingDir, "config")
        compilingServiceConfigsDir = os.path.join(compilingDir, "serviceConfigs")
        dataDir = os.path.join(compilingDir, "data")

        logging.debug("Snapshot compiling dir: %s", compilingDir)
        logging.debug("Snapshot configs compiling dir: %s", compilingConfigsDir)

        snapshotLocation = mainCM.getConfigVal("snapshots.location")
        snapshotArchiveName = "{}/{}.tar.{}".format(snapshotLocation, snapshotName, compressionAlg)

        try:
            os.makedirs(compilingConfigsDir)
            os.makedirs(compilingServiceConfigsDir)
            os.makedirs(dataDir)
            os.makedirs(snapshotLocation, exist_ok=True)
        except Exception as e:
            logging.error("Failed to create directories necessary for snapshot taking: %s", e)
            return False, str(e)

        ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

        try:
            # https://stackoverflow.com/questions/12683834/how-to-copy-directory-recursively-in-python-and-overwrite-all
            shutil.copytree(ScriptInfo.CONFIG_DIR, compilingConfigsDir, dirs_exist_ok=True)
            shutil.copytree(ScriptInfo.SERVICE_CONFIG_DIR, compilingServiceConfigsDir, dirs_exist_ok=True)

            # TODO:: run
            logging.info("Running individual snapshots.")
            for filename in os.listdir(ScriptInfo.SNAPSHOT_SCRIPTS_LOC):
                file = os.path.join(ScriptInfo.SNAPSHOT_SCRIPTS_LOC, filename)
                logging.info("Running script %s", file)
                result = subprocess.run([file, "--snapshot", "-d", compilingDir], shell=False, capture_output=True, text=True, check=True)
                if result.returncode != 0:
                    logging.error("FAILED to run snapshot script, returned %d. Erring script: %s", result.returncode, file)
                    logging.debug("Erring script err output: %s", result.stderr)
                    break
        except Exception as e:
            logging.error("FAILED to compile files for snapshot: %s", e)
            return False, str(e)
        finally:
            ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)

        # https://docs.python.org/3.8/library/tarfile.html#tarfile.TarFile.add
        logging.info("Archiving snapshot bundle.")
        start = time.time()
        try:
            with tarfile.open(snapshotArchiveName, "x:" + compressionAlg) as tar:
                tar.add(compilingDir, arcname="")
        except Exception as e:
            logging.error("FAILED to write files to archive: %s", e)
            # TODO:: remove archive, if exists
            return False, str(e)
        logging.info("Completed archiving snapshot bundle. Took %s seconds", time.time() - start)
        # TODO:: remove compiling dir

        # remove extra files
        filenames = [entry.name for entry in sorted(os.scandir(snapshotLocation),
                                                    key=lambda x: x.stat().st_mtime, reverse=True)]
        filenames = list(filter(lambda curFile: not os.path.isdir(snapshotLocation + "/" + curFile), filenames))

        logging.info("Current files in snapshot dir (%s): %s", len(filenames), ", ".join(filenames))
        for curFile in filenames[5:]:
            logging.info("REMOVING excess file %s", curFile)
            os.remove(snapshotLocation + "/" + curFile)

        logging.info("Done Performing snapshot.")
        return True, snapshotArchiveName

    @staticmethod
    def restoreFromSnapshot(snapshotFile: str) -> bool:
        logging.info("Performing snapshot Restore.")
        ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

        extractionDir = ScriptInfo.TMP_DIR + "/snapshot-restore/" + os.path.basename(snapshotFile).split('.')[0]

        with tarfile.open(snapshotFile, "w:gz") as tar:
            tar.extractall(extractionDir)
        # TODO:: move files to relevant locations

        ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)
        logging.info("Done Performing snapshot Restore.")
        return True

    @staticmethod
    def enableAutomatic():
        CronUtils.enableCron(
            SnapshotUtils.CRON_NAME,
            "oqm-captain --take-snapshot " + SnapshotTrigger.scheduled.name,
            CronFrequency[mainCM.getConfigVal("snapshots.frequency")]
        )

    @staticmethod
    def disableAutomatic():
        CronUtils.disableCron(SnapshotUtils.CRON_NAME)

    @staticmethod
    def isAutomaticEnabled() -> bool:
        return CronUtils.isCronEnabled(SnapshotUtils.CRON_NAME)
