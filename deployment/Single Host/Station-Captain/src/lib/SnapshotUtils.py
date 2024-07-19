import time
from enum import Enum
from ConfigManager import *
from ServiceUtils import *
from CronUtils import *
from CertsUtils import *
import logging
import subprocess
import datetime
import os
import shutil
import tarfile
from LogUtils import *

log = LogUtils.setupLogger(__name__)

class SnapshotTrigger(Enum):
    manual = 1
    scheduled = 2
    preemptive = 3


class SnapshotUtils:
    """

    """
    CRON_NAME = "take-snapshot"

    @staticmethod
    def performSnapshot(snapshotTrigger: SnapshotTrigger) -> (bool, str):
        """
        :param snapshotTrigger:
        :return:
        """
        log.info("Performing snapshot.")

        compressionAlg = mainCM.getConfigVal("snapshots.compressionAlg")
        if all(curAlg not in compressionAlg for curAlg in ["xz", "gz", "bz2"]):
            return False, "Configured compression algorithm was invalid."

        snapshotName = "OQM-snapshot-{}-{}".format(
            datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"),
            snapshotTrigger.name
        )
        log.debug("Snapshot name: %s", snapshotName)
        compilingDir = ScriptInfo.TMP_DIR + "/snapshots/" + snapshotName
        compilingConfigsDir = os.path.join(compilingDir, "config/configs")
        compilingSecretsDir = os.path.join(compilingDir, "config/secrets")
        compilingCertsDir = os.path.join(compilingDir, "certs")
        compilingServiceConfigsDir = os.path.join(compilingDir, "serviceConfigs")
        compilingDataDir = os.path.join(compilingDir, "data")

        log.debug("Snapshot compiling dir: %s", compilingDir)
        log.debug("Snapshot configs compiling dir: %s", compilingConfigsDir)

        snapshotLocation = mainCM.getConfigVal("snapshots.location")
        snapshotArchiveName = "{}/{}.tar.{}".format(snapshotLocation, snapshotName, compressionAlg)

        try:
            try:
                os.makedirs(compilingConfigsDir)
                os.makedirs(compilingSecretsDir)
                os.makedirs(compilingCertsDir)
                os.makedirs(compilingServiceConfigsDir)
                os.makedirs(compilingDataDir)
                os.makedirs(snapshotLocation, exist_ok=True)
            except Exception as e:
                log.error("Failed to create directories necessary for snapshot taking: %s", e)
                return False, str(e)

            ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

            success = False
            try:
                # https://stackoverflow.com/questions/12683834/how-to-copy-directory-recursively-in-python-and-overwrite-all
                shutil.copytree(ScriptInfo.CONFIG_DIR + "/configs", compilingConfigsDir, dirs_exist_ok=True)
                shutil.copytree(ScriptInfo.CONFIG_DIR + "/secrets", compilingSecretsDir, dirs_exist_ok=True)
                shutil.copytree(ScriptInfo.SERVICE_CONFIG_DIR, compilingServiceConfigsDir, dirs_exist_ok=True)
                # copy certs
                certs = mainCM.getConfigVal("cert.certs")
                for cert in certs:
                    certPath = certs[cert]
                    # print(f"Cert: {cert}/{certPath}")
                    if "Pass" in cert:
                        continue
                    shutil.copyfile(certPath, compilingCertsDir + "/" + cert)

                log.info("Running individual snapshots.")
                for filename in os.listdir(ScriptInfo.SNAPSHOT_SCRIPTS_LOC):
                    file = os.path.join(ScriptInfo.SNAPSHOT_SCRIPTS_LOC, filename)
                    log.info("Running script %s", file)
                    result = subprocess.run([file, "--snapshot", "-d", compilingDir], shell=False, capture_output=True, text=True, check=True)
                    if result.returncode != 0:
                        log.error("FAILED to run snapshot script, returned %d. Erring script: %s\nError: %s", result.returncode, file, result.stderr)
                        log.debug("Erring script err output: %s", result.stderr)
                        break
            except Exception as e:
                log.error("FAILED to compile files for snapshot: %s", e)
                return False, str(e)
            finally:
                ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)

            # https://docs.python.org/3.8/library/tarfile.html#tarfile.TarFile.add
            log.info("Archiving snapshot bundle.")
            start = time.time()
            try:
                with tarfile.open(snapshotArchiveName, "x:" + compressionAlg) as tar:
                    tar.add(compilingDir, arcname="")
            except Exception as e:
                log.error("FAILED to write files to archive: %s", e)
                return False, str(e)
            log.info("Completed archiving snapshot bundle. Took %s seconds", time.time() - start)
            success = True

            # remove extra files
            numToKeep = mainCM.getConfigVal("snapshots.numToKeep")
            log.debug("Value for number of snapshots to keep: %s (%s)", numToKeep, type(numToKeep))
            if type(numToKeep) is not int:
                log.warning("snapshots.numToKeep was an invalid value (%s), defaulting to 5.", numToKeep)
                numToKeep = 5
            if numToKeep > 0:
                log.info("Pairing down number of files in snapshot destination to %s", numToKeep)
                filenames = [entry.name for entry in sorted(os.scandir(snapshotLocation),
                                                            key=lambda x: x.stat().st_mtime, reverse=True)]
                filenames = list(filter(lambda curFile: not os.path.isdir(snapshotLocation + "/" + curFile), filenames))
                log.debug("Current files in snapshot dir (%s): %s", len(filenames), ", ".join(filenames))
                for curFile in filenames[5:]:
                    log.info("REMOVING excess file %s", curFile)
                    os.remove(snapshotLocation + "/" + curFile)
            else:
                log.info("Skipping pairing down number of files in snapshot destination.")
            log.info("Done Performing snapshot.")
            return True, snapshotArchiveName
        finally:
            log.info("Cleaning up after snapshot operations")

            try:
                if not success:
                    log.debug("Removing archive file.")
                    if os.path.exists(snapshotArchiveName):
                        os.remove(snapshotArchiveName)
                log.debug("Removing compiling dir.")
                shutil.rmtree(compilingDir)
                log.info("Finished cleaning up after snapshot.")
            except Exception as e:
                log.error("Failed to clean up after performing snapshot operation: %s", e)

    @staticmethod
    def restoreFromSnapshot(snapshotFile: str) -> bool:
        log.info("Performing snapshot Restore.")
        snapshotName = os.path.basename(snapshotFile).split('.')[0]
        extractionDir = ScriptInfo.TMP_DIR + "/snapshot-restore/" + snapshotName

        try:
            log.info("Extracting files from archive.")
            with tarfile.open(snapshotFile, "r:*") as tar:
                tar.extractall(extractionDir)
            log.info("Files extracted successfully.")

            ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

            log.info("Copying in secrets and configs.")
            shutil.copytree(extractionDir + "/config/configs", ScriptInfo.CONFIG_DIR + "/configs", dirs_exist_ok=True)
            shutil.copytree(extractionDir + "/config/secrets", ScriptInfo.CONFIG_DIR + "/secrets", dirs_exist_ok=True)
            shutil.copytree(extractionDir + "/serviceConfigs", ScriptInfo.SERVICE_CONFIG_DIR, dirs_exist_ok=True)

            log.info("Running individual restore.")
            for filename in os.listdir(ScriptInfo.SNAPSHOT_SCRIPTS_LOC):
                file = os.path.join(ScriptInfo.SNAPSHOT_SCRIPTS_LOC, filename)
                log.info("Running script %s", file)
                result = subprocess.run([file, "--restore", "-d", extractionDir], shell=False, capture_output=True, text=True, check=True)
                if result.returncode != 0:
                    log.error("FAILED to run snapshot restore script, returned %d. Erring script: %s\nError: %s", result.returncode, file, result.stderr)
                    log.debug("Erring script err output: %s", result.stderr)

            mainCM.rereadConfigData()

            # load in certs
            certs = mainCM.getConfigVal("cert.certs")
            for cert in certs:
                if "Pass" in cert:
                    continue
                # TODO:: broke here
                shutil.copyfile(extractionDir + "/certs/" + cert, certs[cert])
            CertsUtils.ensureCaInstalled(True)

            ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)
        finally:
            log.info("Cleaning up after snapshot restore.")
            if os.path.exists(extractionDir):
                shutil.rmtree(extractionDir)

        log.info("Done Performing snapshot Restore.")
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
