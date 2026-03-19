import time
from enum import Enum
from ConfigManager import *
from CronUtils import *
from CertsUtils import *
from SnapshotShared import *
import logging
import subprocess
import datetime
import os
import pathlib
import shutil
import tarfile
import boto3
from LogUtils import *
from botocore.config import Config
from botocore.client import BaseClient
from abc import ABC, abstractmethod


class SyncMethod(Enum):
    local = 1
    ssh = 2
    objStorage = 3
    backupSubService = 3


class SyncImpl:
    log = LogUtils.setupLogger("SyncImpl")

    @staticmethod
    def getSnapshotFilePath(snapshotFile: str) -> str:
        return mainCM.getConfigVal("snapshots.location") + "/" + snapshotFile

    @staticmethod
    @abstractmethod
    def getDestFilePath(snapshotFile: str) -> str:
        pass  # This is an abstract method, no implementation here.

    @classmethod
    def sync(cls) -> (bool, str):
        cls.log.info("Synchronizing snapshot files to destination via %s.", cls.method().name)
        previousRemoteFiles: list[str] = cls.listFiles()
        curSnapshotFiles: list[str] = SnapshotSharedUtils.listSnapshots()

        movedFiles = []
        alreadyPresentFiles = []
        removeFromDestination = list(filter(lambda x: x not in curSnapshotFiles, previousRemoteFiles))

        for curSnapshotFile in curSnapshotFiles:
            if curSnapshotFile in previousRemoteFiles:
                cls.log.debug("File already present in destination: %s", curSnapshotFile)
                alreadyPresentFiles.append(curSnapshotFile)
                continue
            cls.addFile(curSnapshotFile)
            movedFiles.append(curSnapshotFile)
        cls.log.info("Done adding files to destination.")
        cls.log.debug("Files moved: %s", movedFiles)

        cls.log.info("Removing extra files from destination.")
        for curFile in removeFromDestination:
            cls.delFile(curFile)
        cls.log.info("Done removing extra files from destination.")
        cls.log.debug("Files removed: %s", removeFromDestination)
        return True, "Successfully synchronized files."

    @staticmethod
    @abstractmethod
    def method() -> SyncMethod:
        pass  # This is an abstract method, no implementation here.

    @classmethod
    @abstractmethod
    def addFile(cls, fileToAdd: str) -> (bool, str):
        """
        Adds a file to the backup location.
        :param fileToAdd: The filename of the file in the snapshot location to add.
        :return:
        """
        pass  # This is an abstract method, no implementation here.

    @classmethod
    @abstractmethod
    def delFile(cls, fileToDelete: str) -> (bool, str):
        """
        Deletes a file from the backup location.
        :param fileToDelete: The filename of the file in the backup location to delete.
        :return:
        """
        pass  # This is an abstract method, no implementation here.

    @classmethod
    @abstractmethod
    def pullFile(cls, fileToPull: str, destination: str = None) -> (bool, str):
        """
        Pulls a file from the backup location into the snapshot location. This should overwrite a local file if present.
        :param fileToPull: The filename of the file in the backup location to delete.
        :return:
        """
        pass  # This is an abstract method, no implementation here.

    @classmethod
    @abstractmethod
    def listFiles(cls) -> list[str]:
        """
        :return: A list of strings of the relevant filenames in the backup location, no parent directories.
        """
        pass  # This is an abstract method, no implementation here.


class LocalSync(SyncImpl):

    log = LogUtils.setupLogger("LocalSync")

    @staticmethod
    def getDestFilePath(snapshotFile: str) -> str:
        return mainCM.getConfigVal("snapshots.backup.local.path") + "/" + snapshotFile

    @staticmethod
    def method() -> SyncMethod:
        return SyncMethod.local

    @classmethod
    def addFile(cls, fileToAdd: str) -> (bool, str):
        destFile = cls.getDestFilePath(fileToAdd)
        pathlib.Path(os.path.dirname(destFile)).mkdir(parents=True, exist_ok=True)
        shutil.copy(
            cls.getSnapshotFilePath(fileToAdd),
            destFile
        )
        return True, ""

    @classmethod
    def delFile(cls, fileToDelete: str) -> (bool, str):
        filePath = cls.getDestFilePath(fileToDelete)
        if os.path.exists(filePath):
            os.remove(filePath)
        else:
            cls.log.debug("Tried deleting file that was already not present: %s", filePath)
        return True, ""

    @classmethod
    def pullFile(cls, fileToPull: str, destination: str = None) -> (bool, str):
        # TODO:: respect destination
        shutil.copy(
            cls.getDestFilePath(fileToPull),
            cls.getSnapshotFilePath(fileToPull)
        )
        return True, ""

    @classmethod
    def listFiles(cls) -> list[str]:
        backupPath = mainCM.getConfigVal("snapshots.backup.local.path")
        if not os.path.exists(backupPath):
            return []
        return list(
            filter(
                SnapshotSharedUtils.filterSnapshotFile,
                [
                    entry.name for entry in sorted(
                    os.scandir(backupPath),
                    key=lambda x: x.stat().st_mtime, reverse=True
                )
                ]
            )
        )


class ObjStorageSync(SyncImpl):
    """
    Powered by Boto3: https://pypi.org/project/boto3/

    https://boto3.amazonaws.com/v1/documentation/api/latest/guide/configuration.html#guide-configuration
    """


    log = LogUtils.setupLogger("ObjStorageSync")

    @classmethod
    def __getS3(cls) -> BaseClient:
        cls.log.info("Getting new S3 interaction client")
        kwargs = {
            'aws_access_key_id': mainCM.getConfigVal("snapshots.backup.objStorage.accessKey"),
            'aws_secret_access_key': mainCM.getConfigVal("snapshots.backup.objStorage.secretKey"),
            'config': Config(signature_version='s3v4')
        }

        if mainCM.getConfigVal("snapshots.backup.objStorage.endpointUrl"):
            kwargs['endpoint_url'] = mainCM.getConfigVal("snapshots.backup.objStorage.endpointUrl")
        if mainCM.getConfigVal("snapshots.backup.objStorage.region"):
            kwargs['region_name'] = mainCM.getConfigVal("snapshots.backup.objStorage.region")
        if mainCM.getConfigVal("snapshots.backup.objStorage.skipVerifySsl"):
            cls.log.warning("SETTING TO IGNORE SSL CERT ISSUES WHEN CONNECTING TO S3.")
            kwargs['verify'] = False

        return boto3.resource(
            's3',
            **kwargs
        )

    @classmethod
    def getBucket(cls, client: BaseClient):
        return client.Bucket(mainCM.getConfigVal("snapshots.backup.objStorage.bucket"))


    @staticmethod
    def method() -> SyncMethod:
        return SyncMethod.objStorage

    @staticmethod
    def getDestFilePath(snapshotFile: str) -> str:
        return mainCM.getConfigVal("snapshots.backup.objStorage.prefix") + "/" + snapshotFile

    @classmethod
    def addFile(cls, fileToAdd: str) -> (bool, str):
        osClient = cls.__getS3()
        bucket = cls.getBucket(osClient)

        bucket.upload_file(
            cls.getSnapshotFilePath(fileToAdd),
            cls.getDestFilePath(fileToAdd)
        )
        return True, ""

    @classmethod
    def delFile(cls, fileToDelete: str) -> (bool, str):
        osClient = cls.__getS3()
        bucket = cls.getBucket(osClient)

        bucket.Object(cls.getDestFilePath(fileToDelete)).delete()
        return True, ""

    @classmethod
    def pullFile(cls, fileToPull: str, destination: str = None) -> (bool, str):
        osClient = cls.__getS3()
        bucket = cls.getBucket(osClient)

        # TODO:: respect destination
        bucket.Object(cls.getDestFilePath(fileToPull)).download_file(cls.getSnapshotFilePath(fileToPull))
        return True, ""

    @classmethod
    def listFiles(cls) -> list[str]:
        osClient = cls.__getS3()
        bucket = osClient.Bucket(mainCM.getConfigVal("snapshots.backup.objStorage.bucket"))

        snapshotFiles = []
        for curObj in bucket.objects.filter(Prefix=mainCM.getConfigVal("snapshots.backup.objStorage.prefix")):
            snapshotFiles.append(os.path.basename(curObj.key))

        snapshotFiles = list(filter(lambda curFile : curFile.startsWith(SnapshotSharedUtils.SNAPSHOT_FILE_PREFIX), snapshotFiles))

        return snapshotFiles


class BackupSubServiceSync(SyncImpl):
    log = LogUtils.setupLogger("BackupSubServiceSync")
    OQM_BACKUP_SERVER_BASE_URI = "https://backups.sub.openquartermaster.com/api/sub/backupFiles/"

    @staticmethod
    def getDestFilePath(snapshotFile: str) -> str:
        return mainCM.getConfigVal("snapshots.backup.local.path") + "/" + snapshotFile

    @staticmethod
    def method() -> SyncMethod:
        return SyncMethod.backupSubService

    @classmethod
    def __authTuple(cls):
        return (
            mainCM.getConfigVal("snapshots.backup.backupSubService.subId"),
            mainCM.getConfigVal("snapshots.backup.backupSubService.subSecret")
        )

    @classmethod
    def __initNewUpload(cls, fileToAdd: str)->dict:
        cls.log.info("Initializing new upload for file: %s", fileToAdd)

        localFile = cls.getSnapshotFilePath(fileToAdd)
        # initialize upload
        initData = {
            "fileName": fileToAdd,
            "fileSize": os.path.getsize(localFile)
        }
        initResponse = requests.post(
            url=cls.OQM_BACKUP_SERVER_BASE_URI + "upload/init",
            json=initData,
            auth=cls.__authTuple()
        )

        if initResponse.status_code != 200:
            raise Exception("Failed to initialize upload ("+str(initResponse.status_code)+"): " + initResponse.text)

        return initResponse.json()

    @classmethod
    def __uploadFile(cls, fileToAdd: str, initData: dict)->dict:
        cls.log.info("Initializing new upload for file: %s", fileToAdd)
        localFile = cls.getSnapshotFilePath(fileToAdd)
        chunkSize = initData["maxChunkSize"]
        fileId = initData["fileId"]

        lastResponse = None
        with open(localFile, 'rb') as file: # Open in binary mode for exact byte control
            while True:
                chunk = file.read(chunkSize)
                if not chunk:
                    break

                multipart_form_data = {
                    'chunk': (None, chunk)
                }

                lastResponse = requests.post(
                    url=cls.OQM_BACKUP_SERVER_BASE_URI + "upload/file/" + fileId,
                    files=multipart_form_data,
                    auth=cls.__authTuple()
                )
                if lastResponse.status_code != 200:
                    raise Exception("Failed to upload chunk ("+str(lastResponse.status_code)+"): " + lastResponse.text)
        cls.log.info("Done uploading file.")
        return lastResponse.json()


    @classmethod
    def addFile(cls, fileToAdd: str) -> (bool, str):

        try:
            initData = cls.__initNewUpload(fileToAdd)
            result = cls.__uploadFile(fileToAdd, initData)
        except Exception as e:
            cls.log.exception("Failed to upload file: ")
            return False, "Failed to upload file: " + str(e)

        # TODO:: check result for file size, hash matching

        return True, ""

    @classmethod
    def delFile(cls, fileToDelete: str) -> (bool, str):
        cls.log.info("Deleting file: %s", fileToDelete)
        deleteResponse = requests.delete(
            url=cls.OQM_BACKUP_SERVER_BASE_URI + "file/" + fileToDelete,
            auth=cls.__authTuple()
        )

        if deleteResponse.status_code != 200:
            return False, Exception("Failed to delete file ("+str(deleteResponse.status_code)+"): " + deleteResponse.text)

        return True, ""

    @classmethod
    def pullFile(cls, fileToPull: str, destination: str = None) -> (bool, str):
        cls.log.info("Downloading file: %s", fileToPull)
        pullResponse = requests.get(
            url=cls.OQM_BACKUP_SERVER_BASE_URI + "file/" + fileToPull,
            auth=cls.__authTuple(),
            stream=True
        )
        pullResponse.raise_for_status()

        with open(cls.getDestFilePath(fileToPull), 'wb') as f:
            for chunk in pullResponse.iter_content(chunk_size=8192): # Iterate in chunks (e.g., 8KB)
                f.write(chunk)

        return True, ""

    @classmethod
    def listFiles(cls) -> list[str]:
        cls.log.info("Getting list of files from backup server")
        listResponse = requests.get(
            url=cls.OQM_BACKUP_SERVER_BASE_URI,
            auth=cls.__authTuple()
        )

        if listResponse.status_code != 200:
            return False, Exception("Failed to list files ("+str(listResponse.status_code)+"): " + listResponse.text)

        return listResponse.json()



class SnapshotBackupUtils:
    """

    """
    log = LogUtils.setupLogger("SnapshotBackupUtils")

    @classmethod
    def getSyncImpl(cls):
        syncMethod = mainCM.getConfigVal("snapshots.backup.method")
        if syncMethod == "local":
            return LocalSync
        elif syncMethod == "objStorage" :
            return ObjStorageSync
        elif syncMethod == "backupSubService" :
            return BackupSubServiceSync
        else:
            return None

    @classmethod
    def backupEnabled(cls)->bool:
        return mainCM.getConfigVal("snapshots.backup.enabled")

    @classmethod
    def syncSnapshots(cls) -> (bool, str):
        """

        :return:
        """
        if not cls.backupEnabled():
            cls.log.info("Snapshot backup is disabled.")
            return True, "Disabled"
        cls.log.info("Syncing snapshots.")
        return cls.getSyncImpl().sync()

    @classmethod
    def listBackedupSnapshots(cls) -> list[str]:
        if not cls.backupEnabled():
            cls.log.info("Snapshot backup is disabled.")
            return []
        return cls.getSyncImpl().listFiles()

    @classmethod
    def downloadSnapshot(cls, snapshotFile: str, destination:str = None) -> (bool, str):
        """

        :return:
        """
        if not cls.backupEnabled():
            cls.log.info("Snapshot backup is disabled.")
            return True, "Disabled"
        cls.log.info("Backing up snapshots.")
        return cls.getSyncImpl().pullFile(snapshotFile, destination)

    @classmethod
    def clearSnapshots(cls) -> (bool, str):
        """
        :return:
        """
        if not cls.backupEnabled():
            cls.log.info("Snapshot backup is disabled.")
            return True, "Disabled"
        SnapshotBackupUtils.log.info("Backing up snapshots.")
        si = cls.getSyncImpl()
        for curFile in si.listFiles():
            si.delFile(curFile)
        return True, "Success"


