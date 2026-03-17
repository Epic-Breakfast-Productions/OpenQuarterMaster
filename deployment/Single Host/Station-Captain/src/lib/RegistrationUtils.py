import time
from enum import Enum
from ConfigManager import *
from ServiceUtils import *
from CronUtils import *
from CertsUtils import *
from SnapshotShared import *
from SnapshotBackupUtils import *
from SystemInfoUtils import *
import logging
import subprocess
import datetime
import os
import shutil
import tarfile
from LogUtils import *
import requests


class RegistrationUtils:
    """

    """
    log = LogUtils.setupLogger("RegistrationUtils")
    CRON_NAME = "ping-registration"
    REG_CONFIG_FILE = "70-registration.json"
    REG_BASE_URL = "https://openquartermaster.com/public/instance-registration"
    SNAPSHOT_FILE_TEMPLATE = SnapshotSharedUtils.SNAPSHOT_FILE_PREFIX + "{}-{}"

    @classmethod
    def setupArgParser(cls, subparsers):
        snapshotSubparser = subparsers.add_parser("registration", aliases=["reg"], help="Registration related commands")
        snapshotSubparsers = snapshotSubparser.add_subparsers(dest="containerCommand")

        snapshot_parser = snapshotSubparsers.add_parser("register", help="Registers this system.")
        snapshot_parser.add_argument(dest="registrationId", help="The registration id given from the OQM website. Will display a registration link if not specified. Go to this link, register the instance, and come back here.", default="", nargs='?')
        snapshot_parser.set_defaults(func=cls.registerFromArgs)

        snapshot_parser = snapshotSubparsers.add_parser("ensureInstanceId", aliases=["eid", "instanceId"], help="Ensures that an instance ID is set for this system. If not, it will generate one and save it to the config file.")
        snapshot_parser.set_defaults(func=cls.ensureInstanceId)

        snapshot_parser = snapshotSubparsers.add_parser("ping", help="Sends a ping to to the OQM site to push registration data. This is called periodically by the system when registered. It is not necessary to call this manually.")
        snapshot_parser.set_defaults(func=cls.pingFromArgs)

        snapshot_parser = snapshotSubparsers.add_parser("status", aliases=["stat", "s"], help="Checks the registration status of this system.")
        snapshot_parser.set_defaults(func=cls.statusFromArgs)

        snapshot_parser = snapshotSubparsers.add_parser("clear", help="Clears the registration information from the system.")
        snapshot_parser.add_argument("--clearInstanceId", dest="clearInstanceId", action="store_true", help="The registration id given from the OQM website. Will display a registration link if not specified. Go to this link, register the instance, and come back here.")
        snapshot_parser.set_defaults(func=cls.clearRegFromArgs)

    @classmethod
    def registerFromArgs(cls, args):
        regId = args.registrationId

        if cls.isRegistered():
            print("System is already registered. Exiting.")
            exit(2)

        if not regId:
            print("Registration id not specified. Please visit the following link to get one: ")
            print("\n"+cls.getRegistrationLink()+"\n")
            exit(0)

        secret = input("Enter the registration secret from the OQM website: ")

        success, message = cls.registerSys(regId, secret)
        if not success:
            print("FAILED to register instance: " + message, file=sys.stderr)
            exit(2)

        print("Registration complete. You can manage the registration at the following link: \n"+cls.getManagementLink()+"\n")

    @classmethod
    def ensureInstanceIdFromArgs(cls, args):
        success, message, created, instanceId = cls.ensureInstanceId()
        if not success:
            print("FAILED to ensure instance id: " + message, file=sys.stderr)
            exit(2)
        print(message)


    @classmethod
    def statusFromArgs(cls, args):
        cls.log.info("Checking registration status.")
        if cls.isRegistered():
            print("System is registered.")
        else:
            print("System is not registered.")

    @classmethod
    def clearRegFromArgs(cls, args):
        cls.clearRegistration(resetInstanceId=args.clearInstanceId)
        print("Registration cleared.")

    @classmethod
    def registerSys(cls, regId, newSecret)->(bool, str):
        cls.log.info("Registering system.")

        success, message = cls.pingRegStatus(regId, newSecret)

        if success:
            cls.log.info("Registered system.")
            cls.enableAutomaticPing()

            mainCM.setConfigValInFile("registration.registrationId", regId, cls.REG_CONFIG_FILE)
            mainCM.setSecretValInFile("registration.registrationSecret", newSecret, cls.REG_CONFIG_FILE)
            mainCM.rereadConfigData()

            return True, "Registered"
        else:
            cls.log.info("Failed to register system.")
            return False, "Failed to register: " + message

    @classmethod
    def clearRegistration(cls, resetInstanceId:bool=True):
        cls.log.info("Clearing registration.")
        mainCM.setConfigValInFile("registration.registrationId", None, cls.REG_CONFIG_FILE)
        mainCM.setConfigValInFile("registration.registrationSecret", None, cls.REG_CONFIG_FILE)

        if resetInstanceId:
            mainCM.setConfigValInFile("registration.instanceId", None, cls.REG_CONFIG_FILE)

        mainCM.rereadConfigData()

        cls.disableAutomaticPing()
        cls.log.info("Cleared registration.")

    @classmethod
    def ensureInstanceId(cls) -> (bool, str, bool, str):
        """
        :param args:
        :return: Worked or not, message, true if created here, instance ID
        """
        cls.log.info("Ensuring instance ID.")
        instanceId = mainCM.getConfigVal("registration.instanceId")
        if instanceId:
            cls.log.info("Instance ID already set.")
            return True, "Instance Id present: " + instanceId, False, instanceId
        cls.log.info("Instance ID not set. Generating.")
        instanceId = str(uuid.uuid4())
        cls.log.info("Generated instance ID: %s", instanceId)
        mainCM.setConfigValInFile("registration.instanceId", instanceId, cls.REG_CONFIG_FILE)
        mainCM.rereadConfigData()
        cls.log.info("Instance ID set.")
        return True, "Instance Id Generated: " + instanceId, True, instanceId

    @classmethod
    def isRegistered(cls) -> bool:
        cls.log.info("Checking registration status.")
        #     TODO:: Any smarter? Check actually pinged?
        return mainCM.getConfigVal("registration.registrationSecret") is not None

    @classmethod
    def getRegistrationLink(cls)->str:
        cls.log.info("Getting registration link.")
        cls.ensureInstanceId()
        return cls.REG_BASE_URL + "?newInstanceId=" + mainCM.getConfigVal("registration.instanceId")

    @classmethod
    def getManagementLink(cls):
        cls.log.info("Getting management link.")
        return cls.REG_BASE_URL + "/instance/" + mainCM.getConfigVal("registration.registrationId") + "/enter"

    @classmethod
    def pingFromArgs(cls, args)->(bool, str):
        status, message = cls.pingRegStatus()
        if not status:
            print("FAILED to ping registration: " + message, file=sys.stderr)
            exit(2)

    @classmethod
    def pingRegStatus(cls, regId = None, newSecret = None)->(bool, str):
        cls.log.info("Pinging registration details.")

        if regId is None:
            regId = mainCM.getConfigVal("registration.registrationId")
        if newSecret is None:
            newSecret = mainCM.getConfigVal("registration.registrationSecret")


        packagesTree = PackageManagement.getOqmPackagesTree(notInstalled=False)

        for groupName, packages  in packagesTree.items():
            for packageName, package in packages.items():
                packagesTree[groupName][packageName] = {"version": package['version']}

        data = {
            "deploymentType": "Single Node Host",
            "oemId": SystemInfoUtils.getOemId(),
            "systemDetails": {
                "memory": SystemInfoUtils.getMemSizeGB(),
                "cpuModel": SystemInfoUtils.getCpuModel(),
                "cpuThreadCount": SystemInfoUtils.getCpuCount(),
                "osType": SystemInfoUtils.getOsType(),
                "os" : SystemInfoUtils.getOsFullName(),
                "packageManager": PackageManagement.getSystemPackageManager()
            },
            "oqmSoftwareVersions": packagesTree
        }

        cls.log.info("Sending registration ping data: %s", data)

        result = requests.put(
            cls.REG_BASE_URL + "/instance/" + regId + "/ping",
            json=data,
            headers={
                "Content-Type": "application/json"
            },
            auth=(
                mainCM.getConfigVal("registration.instanceId") + "/" + regId,
                newSecret
            ),
            timeout=10
        )
        if result.status_code != 200:
            cls.log.error("Ping failed with status code %s", result.status_code)
            return False, "Ping failed with status code " + str(result.status_code) + ": " + result.text
        cls.log.info("Ping sent.")
        return True, "Registration ping successful."

    @classmethod
    def enableAutomaticPing(cls):
        CronUtils.enableCron(
            cls.CRON_NAME,
            "oqm-captain registration ping",
            CronFrequency.monthly
        )

    @classmethod
    def disableAutomaticPing(cls):
        CronUtils.disableCron(cls.CRON_NAME)

    @classmethod
    def isAutomaticPingEnabled(cls) -> bool:
        return CronUtils.isCronEnabled(cls.CRON_NAME)
