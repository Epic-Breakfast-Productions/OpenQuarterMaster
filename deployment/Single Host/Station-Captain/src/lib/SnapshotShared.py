import time
from ConfigManager import *
from LogUtils import *
import logging
import subprocess
import datetime
import os
import shutil
import tarfile



class SnapshotSharedUtils:
    """

    """
    log = LogUtils.setupLogger("SnapshotSharedUtils")
    SNAPSHOT_FILE_PREFIX = "OQM-snapshot-"

    @staticmethod
    def filterSnapshotFile(curFile: str) -> any:
        return (not os.path.isdir(mainCM.getConfigVal("snapshots.location") + "/" + curFile)
                and curFile.startswith(SnapshotSharedUtils.SNAPSHOT_FILE_PREFIX)
                )

    @classmethod
    def listSnapshots(cls) -> list[str]:
        return list(
            filter(
                cls.filterSnapshotFile,
                [
                    entry.name for entry in sorted(
                    os.scandir(mainCM.getConfigVal("snapshots.location")),
                    key=lambda x: x.stat().st_mtime, reverse=True
                )
                ]
            )
        )
