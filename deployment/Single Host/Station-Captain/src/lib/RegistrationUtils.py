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
    REG_BASE_URL = "https://openquartermaster.com/instance-registration"
    SNAPSHOT_FILE_TEMPLATE = SnapshotSharedUtils.SNAPSHOT_FILE_PREFIX + "{}-{}"

    @classmethod
    def setupArgParser(cls, subparsers):
        snapshotSubparser = subparsers.add_parser("registration", aliases=["reg"], help="Registration related commands")
        snapshotSubparsers = snapshotSubparser.add_subparsers(dest="containerCommand")

        snapshot_parser = snapshotSubparsers.add_parser("register", help="Registers this system.")
        snapshot_parser.add_argument(dest="registrationId", help="The registration id given from the OQM website. Will display a link of not specified.", default="", nargs='?')
        snapshot_parser.set_defaults(func=cls.registerFromArgs)

        snapshot_parser = snapshotSubparsers.add_parser("ensureInstanceId", aliases=["eid", "instanceId"], help="Ensures that an instance ID is set for this system. If not, it will generate one and save it to the config file.")
        snapshot_parser.set_defaults(func=cls.ensureInstanceId)

        snapshot_parser = snapshotSubparsers.add_parser("removeRegistration", help="Clears the registration state for the system. This will clear the registration data, generating a new instance ID. It will also call the OQM site to remove the registration.")
        snapshot_parser.set_defaults(func=cls.ensureInstanceId)

        snapshot_parser = snapshotSubparsers.add_parser("ping", help="Sends a ping to to the OQM site to push registration data. This is called periodically by the system when registered. It is not necessary to call this manually.")
        snapshot_parser.set_defaults(func=cls.ensureInstanceId)

        snapshot_parser = snapshotSubparsers.add_parser("status", aliases=["stat", "s"], help="Checks the registration status of this system.")
        snapshot_parser.set_defaults(func=cls.statusFromArgs)

    @classmethod
    def registerFromArgs(cls, args):
        regId = args.registrationId

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

    @classmethod
    def removeRegFromArgs(cls, args):
        cls.log.info("Removing registration.")
        if not cls.isRegistered():
            cls.log.info("Not registered. Exiting.")
            print("System not registered. Exiting.", file=sys.stderr)
            exit(2)

        print("This will REMOVE the registration config, and remove the registration entry from the OQM database. This will also clear the instance ID. This action cannot be undone.")
        confirm = input("Are you sure you want to continue? (y/n)")
        if confirm.lower() != "y":
            cls.log.info("User did not confirm. Aborting.")
            print("Aborting.", file=sys.stderr)
            exit(2)

        instanceId = mainCM.getConfigVal("registration.instanceId")
        print("Instance Id: " + mainCM.getConfigVal("registration.instanceId"))
        instanceIdIn = input("Enter the instance ID to confirm: ")
        if instanceIdIn != instanceId:
            cls.log.info("Instance ID did not match. Aborting.")
            print("Instance ID did not match. Aborting.", file=sys.stderr)
            exit(2)

        cls.log.info("User checks cleared. Removing registration.")
        print("Removing registration...")

        cls.clearRegistration()
        print("Registration removed.")

    @classmethod
    def registerSys(cls, regId, newSecret)->(bool, str):
        cls.log.info("Registering system.")
        mainCM.setConfigValInFile("registration.registrationId", newSecret, cls.REG_CONFIG_FILE)
        mainCM.setSecretValInFile("registration.registrationSecret", newSecret, cls.REG_CONFIG_FILE)
        mainCM.rereadConfigData()

        success, message = cls.pingRegStatus()

        if success:
            cls.log.info("Registered system.")
            cls.enableAutomaticPing()
            return True, "Registered"
        else:
            cls.log.info("Failed to register system.")
            return False, "Failed to register: " + message

    @classmethod
    def clearRegistration(cls):
        cls.log.info("Clearing registration.")
        mainCM.setConfigValInFile("registration.registrationId", None, cls.REG_CONFIG_FILE)
        mainCM.setConfigValInFile("registration.registrationSecret", None, cls.REG_CONFIG_FILE)
        mainCM.setConfigValInFile("registration.instanceId", str(uuid.uuid4()), cls.REG_CONFIG_FILE)
        mainCM.rereadConfigData()

        # TODO:: call to OQM site

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
        return mainCM.getConfigVal("registration.registrationSecret") == True
    #     TODO:: Call OQM site?

    @classmethod
    def getRegistrationLink(cls)->str:
        cls.log.info("Getting registration link.")
        cls.ensureInstanceId()
        return cls.REG_BASE_URL + "?newInstanceId=" + mainCM.getConfigVal("registration.instanceId")

    @classmethod
    def getManagementLink(cls):
        cls.log.info("Getting management link.")
        return cls.REG_BASE_URL + "/instance/" + mainCM.getConfigVal("registration.registrationId") + "/manage"

    @classmethod
    def pingRegStatus(cls)->(bool, str):
        cls.log.info("Pinging registration details.")

        packagesTree = PackageManagement.getOqmPackagesTree(notInstalled=False)
        for groupName, packages  in packagesTree.values():
            for packageName, package in packages.values():
                packagesTree[groupName][packageName] = {"version": package['version']}

        data = {
            "deploymentType": "Single Node Host",
            "oem-id": SystemInfoUtils.getOemId(),
            "memory": SystemInfoUtils.getMemSizeGB(),
            "cpuModel": SystemInfoUtils.getCpuModel(),
            "cpuThreadCount": SystemInfoUtils.getCpuCount(),
            "osType": SystemInfoUtils.getOsType(),
            "os" : SystemInfoUtils.getOsFullName(),
            "packageManager": PackageManagement.getSystemPackageManager(),
            "oqmSoftwareVersions": packagesTree
        }

        result = requests.put(
            cls.REG_BASE_URL + "/instance/" + mainCM.getConfigVal("registration.registrationId") + "/ping",
            json=data,
            headers={
                "Content-Type": "application/json"
            },
            auth=(mainCM.getConfigVal("registration.instanceId"), mainCM.getConfigVal("registration.registrationSecret"))
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
