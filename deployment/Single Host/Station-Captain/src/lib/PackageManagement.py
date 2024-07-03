import os
import subprocess
import logging
import platform


class PackageManagement:
    """
    Class to encapsulate methods that deal with package management.

    Helpful tricks:

    See what packages exist:
    grep -h -P -o "^Package: \K.*" /var/lib/apt/lists/deployment.openquartermaster.com_deb-ppa_._Packages | sort -u

    """
    BASE_STATION_PACKAGE = "oqm-core-base+station"
    ALL_OQM = "oqm-*"
    OQM_PLUGINS = "oqm-plugin-*"
    SYSTEM_PACKAGE_MANAGER = None

    @staticmethod
    def getSystemPackageManager() -> str:
        if PackageManagement.SYSTEM_PACKAGE_MANAGER is not None:
            return PackageManagement.SYSTEM_PACKAGE_MANAGER
        logging.debug("Determining the system's package manager.")

        systemReleaseInfo = platform.freedesktop_os_release()
        if ("ID_LIKE" in systemReleaseInfo and systemReleaseInfo['ID_LIKE'].casefold() == "debian".casefold()) or \
                systemReleaseInfo['ID'].casefold() == "Debian".casefold():
            PackageManagement.SYSTEM_PACKAGE_MANAGER = "apt"

        logging.info("Determined system using %s", PackageManagement.SYSTEM_PACKAGE_MANAGER)
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
        logging.debug("Ensuring core components are installed.")
        # TODO:: will likely need updated for yum
        result = PackageManagement.runPackageCommand("list", PackageManagement.BASE_STATION_PACKAGE, "-qq")
        logging.debug("Output of listing core components: " + result.stdout)
        logging.debug("Error Output of listing core components: " + result.stderr)
        return "installed" in result.stdout

    @staticmethod
    def installCore():
        # TODO:: update with error handling, return
        logging.info("Installing core components.")
        # TODO:: will likely need updated for yum
        result = PackageManagement.runPackageCommand("update")
        result = PackageManagement.runPackageCommand("install", PackageManagement.BASE_STATION_PACKAGE, "-y")
        logging.debug("Result of install: " + result.stdout)
        if result.returncode != 0:
            logging.error("FAILED to install core components: %s", result.stderr)

    @staticmethod
    def updateSystem() -> (bool, str):
        if PackageManagement.getSystemPackageManager() == "apt":
            logging.debug("Updating apt cache.")
            result = subprocess.run(["apt-get", "update"], shell=False, capture_output=True, text=True, check=False)
            if result.returncode != 0:
                logging.error("Failed to run update command: %s", result.stderr)
                return False, result.stderr
            logging.debug("Upgrading apt packages.")
            subprocess.run(["clear"], shell=False, capture_output=False, text=True, check=False)
            result = subprocess.run(["apt-get", "dist-upgrade"], shell=False, capture_output=False, text=True,
                                    check=False)
            if result.returncode != 0:
                logging.error("Failed to run upgrade command: %s", result.stderr)
                return False, result.stderr
        if PackageManagement.getSystemPackageManager() == "yum":
            logging.debug("Upgrading yum packages.")
            subprocess.run(["clear"], shell=False, capture_output=False, text=True, check=False)
            result = subprocess.run(["yum", "update"], shell=False, capture_output=False, text=True, check=False)
            if result.returncode != 0:
                logging.error("Failed to run upgrade command: %s", result.stderr)
                return False, result.stderr
        logging.info("Done updating.")
        return True, None

    @staticmethod
    def promptForAutoUpdates() -> (bool, str):
        if "Ubuntu" in platform.version():
            logging.debug("Prompting user through unattended-upgrades.")
            subprocess.run(["dpkg-reconfigure", "-plow", "unattended-upgrades"], shell=False, capture_output=False,
                           text=True, check=True)
            logging.info("Done.")
            # TODO:: doublecheck automatic restart, setting alert email
        else:
            return False, "Unsupported OS to setup auto updates on."
        return True, None

    @staticmethod
    def getOqmPackagesStr(filter: str = ALL_OQM, installed: bool = True, notInstalled: bool = True):
        logging.debug("Getting OQM packages.")
        result = PackageManagement.runPackageCommand("list", filter, "-qq")
        logging.debug("Output of listing core components: " + result.stdout)
        logging.debug("Error Output of listing core components: " + result.stderr)

        result = result.stdout
        output = []
        for curLine in result.splitlines():
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
        return package.split("-")[3].replace("+", " ")

    @staticmethod
    def packageLineToArray(curLine:str) -> (dict):
        output = {}

        output['package'] = curLine.split("/")[0]
        lineParts = curLine.split(" ")
        output['version'] = lineParts[1]
        output['installed'] = "installed" in lineParts[3]

        return output

    @staticmethod
    def getOqmPackagesList(filter: str = ALL_OQM, installed: bool = True, notInstalled: bool = True):
        logging.debug("Getting OQM packages.")
        result = PackageManagement.getOqmPackagesStr(filter, installed, notInstalled)
        result = result.splitlines()
        result = map(PackageManagement.packageLineToArray,result)
        return result

    @staticmethod
    def ensureOnlyPluginsInstalled(pluginList:list) -> (bool, str):
        logging.debug("Ensuring only plugins in list installed.")
        # TODO:: this
