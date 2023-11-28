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
    BASE_STATION_PACKAGE = "open+quarter+master-core-base+station"

    @staticmethod
    def getSystemPackageManager() -> str:
        logging.debug("Determining the system's package manager.")

        # Not supported until Python 3.10
        # systemReleaseInfo = platform.freedesktop_os_release()
        # if systemReleaseInfo['ID_LIKE'] == "debian":
        #     return "apt"

        if "Ubuntu" in platform.version():
            return "apt"

    @staticmethod
    def runPackageCommand(command: str, package: str = None, *options: str) -> subprocess.CompletedProcess:
        args = []
        args.append(PackageManagement.getSystemPackageManager())
        args.append(command)
        args.extend(options)
        if package is not None:
            args.append(package)

        return subprocess.run(args, shell=False, capture_output=True, text=True, check=True)

    @staticmethod
    def coreInstalled() -> bool:
        logging.debug("Ensuring core components are installed.")
        result = PackageManagement.runPackageCommand("list", PackageManagement.BASE_STATION_PACKAGE, "-qq")
        logging.debug("Output of listing core components: " + result.stdout)
        logging.debug("Error Output of listing core components: " + result.stderr)
        return "installed" in result.stdout

    @staticmethod
    def installCore():
        logging.info("Installing core components.")
        result = PackageManagement.runPackageCommand("update")
        result = PackageManagement.runPackageCommand("install", PackageManagement.BASE_STATION_PACKAGE, "-y")
        logging.debug("Result of install: " + result.stdout)

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
            result = subprocess.run(["apt-get", "dist-upgrade"], shell=False, capture_output=False, text=True, check=False)
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
            subprocess.run(["dpkg-reconfigure", "-plow", "unattended-upgrades"], shell=False, capture_output=False, text=True, check=True)
            logging.info("Done.")
            # TODO:: doublecheck automatic restart, setting alert email
        else:
            return False, "Unsupported OS to setup auto updates on."
        return True, None