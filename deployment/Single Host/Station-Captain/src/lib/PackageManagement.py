import os
import subprocess
import platform
import re

from ServiceUtils import *
from LogUtils import *


class PackageManagement:
    """
    Class to encapsulate methods that deal with package management.

    Helpful tricks:

    See what packages exist:
    grep -h -P -o "^Package: .*" /var/lib/apt/lists/deployment.openquartermaster.com_deb-ppa_._Packages | sort -u

    """
    log = LogUtils.setupLogger("PackageManagement")
    BASE_STATION_PACKAGE = "oqm-core-base+station"
    ALL_OQM = "oqm-*"
    OQM_PLUGINS = "oqm-plugin-*"
    SYSTEM_PACKAGE_MANAGER = None

    @staticmethod
    def getSystemPackageManager() -> str:
        if PackageManagement.SYSTEM_PACKAGE_MANAGER is not None:
            return PackageManagement.SYSTEM_PACKAGE_MANAGER
        PackageManagement.log.debug("Determining the system's package manager.")

        systemReleaseInfo = platform.freedesktop_os_release()
        if ("ID_LIKE" in systemReleaseInfo and systemReleaseInfo['ID_LIKE'].casefold() == "debian".casefold()) or \
                systemReleaseInfo['ID'].casefold() == "Debian".casefold():
            PackageManagement.SYSTEM_PACKAGE_MANAGER = "apt"

        PackageManagement.log.info("Determined system using %s", PackageManagement.SYSTEM_PACKAGE_MANAGER)
        return PackageManagement.SYSTEM_PACKAGE_MANAGER

    @staticmethod
    def runPackageCommand(command: str, package: str = None, *options: str) -> subprocess.CompletedProcess:
        args = []
        args.append(PackageManagement.getSystemPackageManager())
        args.append(command)
        args.extend(options)
        if package is not None:
            args.append(package)

        return subprocess.run(args, shell=False, capture_output=True, text=True, check=False)

    @staticmethod
    def coreInstalled() -> bool:
        PackageManagement.log.debug("Ensuring core components are installed.")
        # TODO:: will likely need updated for yum
        result = PackageManagement.runPackageCommand("list", PackageManagement.BASE_STATION_PACKAGE, "-qq")
        PackageManagement.log.debug("Output of listing core components: " + result.stdout)
        PackageManagement.log.debug("Error Output of listing core components: " + result.stderr)
        return "installed" in result.stdout

    @staticmethod
    def installPackages(packages:list) -> (bool, str):
        PackageManagement.log.info("Installing packages: %s", packages)
        command:list = ["apt-get", "install", "-y"]
        command.extend(packages)
        result = subprocess.run(
            command,
            shell=False, capture_output=True, text=True, check=False
        )
        if result.returncode != 0:
            PackageManagement.log.error("Failed to run install packages command: %s", result.stderr)
            return False, result.stderr
        return True, None

    @staticmethod
    def removePackages(packages:list) -> (bool, str):
        PackageManagement.log.info("Removing packages: %s", packages)
        command:list = ["apt-get", "remove", "-y", "--purge"]
        command.extend(packages)
        result = subprocess.run(
            command,
            shell=False, capture_output=True, text=True, check=False
        )
        if result.returncode != 0:
            PackageManagement.log.error("Failed to run remove packages command: %s", result.stderr)
            return False, result.stderr
        return True, None

    @staticmethod
    def installCore() -> tuple[bool, str]:
        PackageManagement.log.info("Installing core components.")
        # TODO:: will likely need updated for yum
        result = PackageManagement.runPackageCommand("update")
        result = PackageManagement.runPackageCommand("install", PackageManagement.BASE_STATION_PACKAGE, "-y")
        PackageManagement.log.debug("Result of install: " + result.stdout)
        if result.returncode != 0:
            PackageManagement.log.error("FAILED to install core components: %s", result.stderr)
            return False, result.stderr
        return True, None

    @staticmethod
    def updateSystem() -> (bool, str):
        if PackageManagement.getSystemPackageManager() == "apt":
            PackageManagement.log.debug("Updating apt cache.")
            result = subprocess.run(["apt-get", "update"], shell=False, capture_output=True, text=True, check=False)
            if result.returncode != 0:
                PackageManagement.log.error("Failed to run update command: %s", result.stderr)
                return False, result.stderr
            PackageManagement.log.debug("Upgrading apt packages.")
            subprocess.run(["clear"], shell=False, capture_output=False, text=True, check=False)
            result = subprocess.run(["apt-get", "dist-upgrade"], shell=False, capture_output=False, text=True,
                                    check=False)
            if result.returncode != 0:
                PackageManagement.log.error("Failed to run upgrade command: %s", result.stderr)
                return False, result.stderr
        if PackageManagement.getSystemPackageManager() == "yum":
            PackageManagement.log.debug("Upgrading yum packages.")
            subprocess.run(["clear"], shell=False, capture_output=False, text=True, check=False)
            result = subprocess.run(["yum", "update"], shell=False, capture_output=False, text=True, check=False)
            if result.returncode != 0:
                PackageManagement.log.error("Failed to run upgrade command: %s", result.stderr)
                return False, result.stderr
        PackageManagement.log.info("Done updating.")
        return True, None

    @staticmethod
    def promptForAutoUpdates() -> (bool, str):
        if "Ubuntu" in platform.version():
            PackageManagement.log.debug("Prompting user through unattended-upgrades.")
            subprocess.run(["dpkg-reconfigure", "-plow", "unattended-upgrades"], shell=False, capture_output=False,
                           text=True, check=True)
            PackageManagement.log.info("Done.")
            # TODO:: doublecheck automatic restart, setting alert email
        else:
            return False, "Unsupported OS to setup auto updates on."
        return True, None

    @staticmethod
    def getOqmPackagesStr(filter: str = ALL_OQM, installed: bool = True, notInstalled: bool = True):
        PackageManagement.log.debug("Getting OQM packages.")
        result = PackageManagement.runPackageCommand("list", filter, "-qq")
        PackageManagement.log.debug("Output of listing core components: " + result.stdout)
        PackageManagement.log.debug("Error Output of listing core components: " + result.stderr)

        result = result.stdout
        output = []
        for curLine in result.splitlines():
            if installed and notInstalled:
                output.append(curLine)
                continue
            if installed:
                if "installed" in curLine:
                    output.append(curLine)
                continue
            if notInstalled:
                if not "installed" in curLine:
                    output.append(curLine)
        return os.linesep.join(output)

    @staticmethod
    def getPluginDisplayName(package:str):
        # print("Package: " + package)
        return package.split("-")[2].replace("+", " ")

    @staticmethod
    def getPackageInfo(package:str) -> (bool, str):
        output = {}
        packageShow = subprocess.run(['apt-cache', 'show', package], shell=False, capture_output=True, text=True, check=False).stdout
        packageShow = packageShow.splitlines()

        for curLine in packageShow:
            if not curLine.strip():
                continue
            split = curLine.split(": ", 1)
            name = split[0]
            value = split[1]
            output[name] = value
        return output

    @staticmethod
    def packageLineToArray(curLine:str) -> (dict):
        output = {}
        # print("cur line: ", curLine)
        output['package'] = curLine.split("/")[0]
        output['displayName'] = PackageManagement.getPluginDisplayName(output['package'])
        lineParts = curLine.split(" ")
        # print("lineParts: ", lineParts)
        output['version'] = lineParts[1]
        output['installed'] = "installed" in curLine

        packageInfo = PackageManagement.getPackageInfo(output['package'])
        # print(packageInfo)
        output['description'] = packageInfo['Description']
        output['fullInfo'] = packageInfo

        return output

    @staticmethod
    def getOqmPackagesList(filter: str = ALL_OQM, installed: bool = True, notInstalled: bool = True):
        PackageManagement.log.debug("Getting OQM packages.")
        result = PackageManagement.getOqmPackagesStr(filter, installed, notInstalled)
        # print("Package list str: " + result)
        result = result.splitlines()
        result = map(PackageManagement.packageLineToArray,result)
        # TODO:: debug
        # print("Package list: ", list(result))
        return result

    @staticmethod
    def ensureOnlyPluginsInstalled(pluginList:list) -> (bool, str):
        PackageManagement.log.debug("Ensuring only plugins in list installed.")

        allInstalledPlugins = map(
            lambda i: i['package'],
            PackageManagement.getOqmPackagesList(PackageManagement.OQM_PLUGINS, installed=True)
        )
        pluginsToRemove = [i for i in allInstalledPlugins if i not in pluginList]

        # TODO Try to figure out how to remove unwanted plugins while not bouncing dependency plugins
        # TODO:: error check
        PackageManagement.removePackages(pluginsToRemove)
        PackageManagement.installPackages(pluginList)

        ServiceUtils.doServiceCommand(ServiceStateCommand.restart, ServiceUtils.SERVICE_ALL)

    @staticmethod
    def checkSnapInstalled():
        packageInfo = PackageManagement.getPackageInfo("snapd")


    @staticmethod
    def checkFirefoxSnapInstalled() -> (bool, str):
        PackageManagement.log.debug("Checking if ")
        snapInfoResult = subprocess.run(['snap', 'info', 'firefox'], shell=False, capture_output=True, text=True, check=False)
        # print("Firefox snap output: " + snapInfoResult.stdout)
        if snapInfoResult.returncode != 0:
            # print("snap command for firefox failed")
            return False

        if re.search("^installed: ", snapInfoResult.stdout, re.MULTILINE):
            return True
        # print("Installed not found.")
        return False
