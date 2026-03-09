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


class SnapshotTrigger(Enum):
    manual = 1
    scheduled = 2
    preemptive = 3


class SnapshotUtils:
    """

    """
    log = LogUtils.setupLogger("SnapshotUtils")
    CRON_NAME = "take-snapshot"
    SNAPSHOT_FILE_TEMPLATE = SnapshotSharedUtils.SNAPSHOT_FILE_PREFIX + "{}-{}"

    @classmethod
    def setupArgParser(cls, subparsers):
        snapshotSubparser = subparsers.add_parser("snapshot", aliases=["snap"], help="Snapshot related commands")
        snapshotSubparsers = snapshotSubparser.add_subparsers(dest="containerCommand")

        snapshot_parser = snapshotSubparsers.add_parser("take-snapshot", help="Triggers a snapshot.")
        snapshot_parser.add_argument(dest="trigger", help="What is triggering the snapshot. Defaults to 'manual'.", choices=["manual", "scheduled", "preemptive"], default="manual", nargs='?')
        snapshot_parser.set_defaults(func=SnapshotUtils.snapshotFromArgs)

    @classmethod
    def snapshotFromArgs(cls, args):
        trigger = SnapshotTrigger[args.trigger]
        success, message = SnapshotUtils.performSnapshot(trigger)
        if not success:
            print("FAILED to create snapshot: " + message, file=sys.stderr)
            exit(2)
        print(message)

    @staticmethod
    def performSnapshot(snapshotTrigger: SnapshotTrigger) -> (bool, str):
        """
        :param snapshotTrigger:
        :return:
        """
        SnapshotUtils.log.info("Performing snapshot.")

        compressionAlg = mainCM.getConfigVal("snapshots.compressionAlg")
        if all(curAlg not in compressionAlg for curAlg in ["xz", "gz", "bz2"]):
            return False, "Configured compression algorithm was invalid."

        snapshotName = SnapshotUtils.SNAPSHOT_FILE_TEMPLATE.format(
            datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"),
            snapshotTrigger.name
        )
        SnapshotUtils.log.debug("Snapshot name: %s", snapshotName)
        compilingDir = ScriptInfo.TMP_DIR + "/snapshots/" + snapshotName
        compilingConfigsDir = os.path.join(compilingDir, "config/configs")
        compilingSecretsDir = os.path.join(compilingDir, "config/secrets")
        compilingCertsDir = os.path.join(compilingDir, "certs")
        compilingServiceConfigsDir = os.path.join(compilingDir, "serviceConfigs")
        compilingDataDir = os.path.join(compilingDir, "data")

        SnapshotUtils.log.debug("Snapshot compiling dir: %s", compilingDir)
        SnapshotUtils.log.debug("Snapshot configs compiling dir: %s", compilingConfigsDir)

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
                SnapshotUtils.log.error("Failed to create directories necessary for snapshot taking: %s", e)
                return False, str(e)

            ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

            success = False
            try:
                # https://stackoverflow.com/questions/12683834/how-to-copy-directory-recursively-in-python-and-overwrite-all
                shutil.copytree(ScriptInfo.CONFIG_DIR + "/configs", compilingConfigsDir, dirs_exist_ok=True)
                shutil.copytree(ScriptInfo.CONFIG_DIR + "/secrets", compilingSecretsDir, dirs_exist_ok=True)
                shutil.copytree(ScriptInfo.SERVICE_CONFIG_DIR, compilingServiceConfigsDir, dirs_exist_ok=True)
                # copy certs
                certs = mainCM.getConfigVal("cert.selfSigned.certs")
                for cert in certs:
                    certPath = certs[cert]
                    # print(f"Cert: {cert}/{certPath}")
                    if "Pass" in cert:
                        continue
                    if not os.path.exists(certPath):
                        continue
                    shutil.copyfile(certPath, compilingCertsDir + "/" + cert)
                if mainCM.getConfigVal("cert.provided.enabled"):
                    shutil.copyfile(mainCM.getConfigVal("cert.provided.cert"), compilingCertsDir + "/providedCert.crt")
                    shutil.copyfile(mainCM.getConfigVal("cert.provided.key"), compilingCertsDir + "/providedCertKey.pem")

                SnapshotUtils.log.info("Running individual snapshots.")
                for filename in os.listdir(ScriptInfo.SNAPSHOT_SCRIPTS_LOC):
                    file = os.path.join(ScriptInfo.SNAPSHOT_SCRIPTS_LOC, filename)
                    SnapshotUtils.log.info("Running script %s", file)
                    result = subprocess.run([file, "--snapshot", "-d", compilingDir], shell=False, capture_output=True, text=True, check=True)
                    if result.returncode != 0:
                        SnapshotUtils.log.error("FAILED to run snapshot script, returned %d. Erring script: %s\nError: %s", result.returncode, file, result.stderr)
                        SnapshotUtils.log.debug("Erring script err output: %s", result.stderr)
                        break
            except Exception as e:
                SnapshotUtils.log.error("FAILED to compile files for snapshot: %s", e)
                return False, str(e)
            finally:
                ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)

            # https://docs.python.org/3.8/library/tarfile.html#tarfile.TarFile.add
            SnapshotUtils.log.info("Archiving snapshot bundle.")
            start = time.time()
            try:
                with tarfile.open(snapshotArchiveName, "x:" + compressionAlg) as tar:
                    tar.add(compilingDir, arcname="")
            except Exception as e:
                SnapshotUtils.log.error("FAILED to write files to archive: %s", e)
                return False, str(e)
            SnapshotUtils.log.info("Completed archiving snapshot bundle. Took %s seconds", time.time() - start)
            success = True

            if mainCM.getConfigVal("snapshots.encryption.enabled"):
                SnapshotUtils.log.info("Encrypting resulting bundle...")
                origArchive = snapshotArchiveName
                snapshotArchiveName = snapshotArchiveName + ".enc"
                result = subprocess.run(
                    [
                        "openssl", "enc", "-e",
                        "-in", origArchive, "-out", snapshotArchiveName,
                        "-pass", "pass:" + mainCM.getConfigVal("snapshots.encryption.pass"),
                        "-pbkdf2"
                    ],
                    shell=False, capture_output=True, text=True, check=False
                )
                SnapshotUtils.log.info("Finished encrypting snapshot.")
                if result.returncode != 0:
                    SnapshotUtils.log.error("FAILED to encrypt resulting archive. Returned: %d. Erring script: %s\nError: %s", result.returncode, file, result.stderr)
                    SnapshotUtils.log.debug("Erring script err output: %s", result.stderr)
                    return (False, "Error encrypting archive: " + result.stderr)
                os.remove(origArchive)

            # remove extra files
            numToKeep = mainCM.getConfigVal("snapshots.numToKeep")
            SnapshotUtils.log.debug("Value for number of snapshots to keep: %s (%s)", numToKeep, type(numToKeep))
            if type(numToKeep) is not int:
                SnapshotUtils.log.warning("snapshots.numToKeep was an invalid value (%s), defaulting to 5.", numToKeep)
                numToKeep = 5
            if numToKeep > 0:
                SnapshotUtils.log.info("Pairing down number of files in snapshot destination to %s", numToKeep)
                filenames = SnapshotSharedUtils.listSnapshots()

                SnapshotUtils.log.debug("Current files in snapshot dir (%s): %s", len(filenames), ", ".join(filenames))
                for curFile in filenames[5:]:
                    SnapshotUtils.log.info("REMOVING excess file %s", curFile)
                    os.remove(snapshotLocation + "/" + curFile)
            else:
                SnapshotUtils.log.info("Skipping pairing down number of files in snapshot destination.")
            SnapshotUtils.log.info("Done Performing snapshot.")

            if SnapshotBackupUtils.backupEnabled():
                SnapshotBackupUtils.syncSnapshots()
            else:
                SnapshotUtils.log.info("Backups are disabled. Skipping.")

            return True, snapshotArchiveName
        finally:
            SnapshotUtils.log.info("Cleaning up after snapshot operations")

            try:
                if not success:
                    SnapshotUtils.log.debug("Removing archive file.")
                    if os.path.exists(snapshotArchiveName):
                        os.remove(snapshotArchiveName)
                    if os.path.exists(snapshotArchiveName + '.enc'):
                        os.remove(snapshotArchiveName + '.enc')
                SnapshotUtils.log.debug("Removing compiling dir.")
                shutil.rmtree(compilingDir)
                SnapshotUtils.log.info("Finished cleaning up after snapshot.")
            except Exception as e:
                SnapshotUtils.log.error("Failed to clean up after performing snapshot operation: %s", e)

    @staticmethod
    def restoreFromSnapshot(snapshotFile: str, decryptPass: str = None) -> (bool, str):
        SnapshotUtils.log.info("Performing snapshot Restore.")
        snapshotName = os.path.basename(snapshotFile).split('.')[0]
        extractionDir = ScriptInfo.TMP_DIR + "/snapshot-restore/" + snapshotName
        # create extraction dir
        os.makedirs(extractionDir)

        try:
            if snapshotFile.endswith(".enc"):
                SnapshotUtils.log.info("Snapshot is encrypted. Decrypting...")
                # TODO:: verify can extract to same location as file exists
                encryptedFile = snapshotFile
                snapshotFile = extractionDir + "/" + os.path.basename(snapshotFile).replace(".enc", "")
                result = subprocess.run(
                    [
                        "openssl", "enc", "-d",
                        "-in", encryptedFile, "-out", snapshotFile,
                        "-pass", "pass:" + mainCM.getConfigVal("snapshots.encryption.pass"),
                        "-pbkdf2"
                    ],
                    shell=False, capture_output=True, text=True, check=True
                )
                SnapshotUtils.log.info("Finished encrypting snapshot.")
                if result.returncode != 0:
                    SnapshotUtils.log.error("FAILED to decrypt archive. Returned: %d. File: %s\nError: %s", result.returncode, encryptedFile, result.stderr)
                    SnapshotUtils.log.debug("Erring script err output: %s", result.stderr)
                    return False, "Error decrypting archive: " + result.stderr

            SnapshotUtils.log.info("Extracting files from archive.")
            with tarfile.open(snapshotFile, "r:*") as tar:
                tar.extractall(extractionDir)
            SnapshotUtils.log.info("Files extracted successfully.")

            ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

            SnapshotUtils.log.info("Copying in secrets and configs.")
            shutil.copytree(extractionDir + "/config/configs", ScriptInfo.CONFIG_DIR + "/configs", dirs_exist_ok=True)
            shutil.copytree(extractionDir + "/config/secrets", ScriptInfo.CONFIG_DIR + "/secrets", dirs_exist_ok=True)
            shutil.copytree(extractionDir + "/serviceConfigs", ScriptInfo.SERVICE_CONFIG_DIR, dirs_exist_ok=True)

            SnapshotUtils.log.info("Running individual restore scripts.")
            for filename in os.listdir(ScriptInfo.SNAPSHOT_SCRIPTS_LOC):
                file = os.path.join(ScriptInfo.SNAPSHOT_SCRIPTS_LOC, filename)
                SnapshotUtils.log.info("Running script %s", file)
                result = subprocess.run([file, "--restore", "-d", extractionDir], shell=False, capture_output=True, text=True, check=True)
                if result.returncode != 0:
                    SnapshotUtils.log.error("FAILED to run snapshot restore script, returned %d. Erring script: %s\nError: %s", result.returncode, file, result.stderr)
                    SnapshotUtils.log.debug("Erring script err output: %s", result.stderr)
            SnapshotUtils.log.info("DONE Running individual restore scripts.")
            mainCM.rereadConfigData()

            SnapshotUtils.log.info("Restoring certs from snapshot data...")
            # load in certs
            certs = mainCM.getConfigVal("cert.selfSigned.certs")
            for cert in certs:
                if "Pass" in cert:
                    continue
                if not os.path.exists(extractionDir + "/certs/" + cert):
                    continue
                # TODO:: broke here
                shutil.copyfile(extractionDir + "/certs/" + cert, certs[cert])
            CertsUtils.ensureCaInstalled(True)
            if mainCM.getConfigVal("cert.provided.enabled"):
                shutil.copyfile(extractionDir + "/certs/providedCert.crt", mainCM.getConfigVal("cert.provided.cert"))
                shutil.copyfile(extractionDir + "/certs/providedCertKey.pem", mainCM.getConfigVal("cert.provided.key"))
            SnapshotUtils.log.info("DONE Restoring certs.")

            ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)
        finally:
            SnapshotUtils.log.info("Cleaning up after snapshot restore.")
            if os.path.exists(extractionDir):
                shutil.rmtree(extractionDir)

        SnapshotUtils.log.info("Done Performing snapshot Restore.")
        return True, ""

    @staticmethod
    def enableAutomatic():
        CronUtils.enableCron(
            SnapshotUtils.CRON_NAME,
            "oqm-captain snapshot take-snapshot " + SnapshotTrigger.scheduled.name,
            CronFrequency[mainCM.getConfigVal("snapshots.frequency")]
        )

    @staticmethod
    def disableAutomatic():
        CronUtils.disableCron(SnapshotUtils.CRON_NAME)

    @staticmethod
    def isAutomaticEnabled() -> bool:
        return CronUtils.isCronEnabled(SnapshotUtils.CRON_NAME)
