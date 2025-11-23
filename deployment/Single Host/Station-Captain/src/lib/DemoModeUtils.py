import time
from enum import Enum
from ConfigManager import *
from ScriptInfos import *
from CronUtils import *
from CertsUtils import *
from SnapshotUtils import *
import logging
import subprocess
import datetime
import os
from pathlib import *
import shutil
from LogUtils import *
import argparse


class DemoModeUtils:
    """

    """
    log = LogUtils.setupLogger("DemoModeUtils")
    CRON_NAME = "demo-reset"

    @classmethod
    def setupArgParser(cls, subparsers):
        demoSubparser = subparsers.add_parser("demo", help="Demo mode related commands")
        demoSubparsers = demoSubparser.add_subparsers(dest="containerCommand")

        snapshot_parser = demoSubparsers.add_parser("enable", help="Enables demo mode.")
        snapshot_parser.add_argument(dest="snapshotFile", help="The snapshot file to use to restore from.", type=argparse.FileType('r'))
        snapshot_parser.set_defaults(func=DemoModeUtils.enableFromArgs)

        snapshot_parser = demoSubparsers.add_parser("disable", help="Disables demo mode.")
        snapshot_parser.set_defaults(func=DemoModeUtils.disableFromArgs)

        snapshot_parser = demoSubparsers.add_parser("doDemoReset", help="Performs the demo reset.")
        snapshot_parser.set_defaults(func=DemoModeUtils.doDemoResetFromArgs)


    @classmethod
    def enableFromArgs(cls, args):
        cls.enable(
            args.snapshotFile.name
        )

    @classmethod
    def disableFromArgs(cls, args):
        cls.disable()

    @classmethod
    def doDemoResetFromArgs(cls, args):
        cls.doDemoReset()

    @classmethod
    def doDemoReset(cls):
        snapshotFile = mainCM.getConfigVal("captain.demoMode.snapshotFile")
        cls.log.info("Demo mode resetting from snapshot file: %s", snapshotFile)
        SnapshotUtils.restoreFromSnapshot(snapshotFile)

    @classmethod
    def enable(cls, snapshotFilePath:str):
        cls.log.info("Enabling demo mode with file: %s", snapshotFilePath)
        mainCM.setConfigValInFile(
            "captain.demoMode.snapshotFile", snapshotFilePath, ScriptInfo.CONFIG_DEFAULT_UPDATE_FILE)
        cls.enableCron()

    @classmethod
    def enableCron(cls):
        CronUtils.enableCron(
            cls.CRON_NAME,
            "oqm-captain demo doDemoReset",
            CronFrequency[mainCM.getConfigVal("captain.demoMode.frequency")]
        )

    @classmethod
    def disable(cls):
        CronUtils.disableCron(cls.CRON_NAME)

    @classmethod
    def isAutomaticEnabled(cls) -> bool:
        return CronUtils.isCronEnabled(cls.CRON_NAME)

