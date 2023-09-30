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
            return "apt-get"

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
    def updateSystem():
        logging.debug("Updating the system.")
        result = PackageManagement.runPackageCommand("update")
        result = PackageManagement.runPackageCommand("dist-upgrade", None, "-y")
        logging.info("Result of update: " + result.stdout)
