from dialog import Dialog
from ConfigManager import *
from PackageManagement import *
import time


class UserInteraction:
    """

    User interaction functions.

    References:
        - https://pythondialog.sourceforge.io
    """
    WIDE_WIDTH = 200

    def __init__(self):
        self.dialog = Dialog(
            dialog="dialog",
            # autowidgetsize=True
        )
        self.dialog.set_background_title(ScriptInfo.SCRIPT_TITLE)

    @staticmethod
    def clearScreen():
        os.system('clear')

    def startUserInteraction(self):
        self.userInteractionSetupCheck()
        self.mainMenu()

    def userInteractionSetupCheck(self):
        logging.info("Checking system is setup")
        if not PackageManagement.coreInstalled():
            logging.info("Core setup not installed")
            code = self.dialog.yesno("Core components are not installed. Install now?", title="Setup")
            if code == self.dialog.OK:
                PackageManagement.installCore()
        else:
            logging.info("Core components already installed.")

    def mainMenu(self):
        logging.debug("Running main menu.")
        while True:
            code, choice = self.dialog.menu(
                "Please choose an option:",
                title="Main Menu",
                choices=[
                    ("(1)", "Info / Status"),
                    ("(2)", "Manage Installation"),
                    ("(3)", "Snapshots"),
                    ("(4)", "Updates"),
                    ("(5)", "Cleanup"),
                    ("(6)", "Captain Settings"),
                ]
            )
            UserInteraction.clearScreen()
            logging.debug('Main menu choice: %s, code: %s', choice, code)
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                self.infoStatusMenu()
            if choice == "(2)":
                self.manageInstallationMenu()

        logging.debug("Done running main menu.")

    def infoStatusMenu(self):
        logging.debug("Running information stats menu.")
        while True:
            code, choice = self.dialog.menu(
                "Please choose an option:",
                title="Information and Status",
                choices=[
                    ("(1)", "Installation status"),
                    ("(2)", "Host information"),
                ]
            )
            self.clearScreen()
            logging.debug('Main menu choice: %s, code: %s', choice, code)
            if code != self.dialog.OK:
                break
            if choice == "(1)":
                self.showSystemStatus()
            if choice == "(2)":
                self.showHostInfo()

        logging.debug("Done running info and stats selection menu.")

    def showHostInfo(self):
        logging.debug("Showing host info.")

        textToShow = ""
        try:
            self.dialog.gauge_start("Getting networking info...", title="Retrieving system information", percent=0)
            ipAddrs = subprocess.run(["hostname", "-I"], shell=False, capture_output=True, text=True, check=True).stdout
            ipAddrs = (subprocess.run(["hostname"], shell=False, capture_output=True, text=True,
                                      check=True).stdout.replace("\n", "") + ".local " + ipAddrs)
            ipAddrs = ipAddrs.replace(" ", "\n\t")
            textToShow += "Hostname and IP addresses:\n\t" + ipAddrs
            textToShow += "\tHostname set in configuration:\n\t\t" + mainCM.getConfigVal("system.hostname",
                                                                                         mainCM.configData)
            textToShow += "\n\n\n"

            self.dialog.gauge_update(20, "Getting OS info...", True)
            with open("/etc/os-release") as file:
                osInfo = file.read() + "\n"
            osInfo += subprocess.run(["uname", "-a"], shell=False, capture_output=True, text=True, check=True).stdout
            textToShow += "OS Info:\n" + osInfo + "\n\n\n"

            self.dialog.gauge_update(40, "Getting Hardware info...", True)
            hwinfo = subprocess.run(["hwinfo", "--short"], shell=False, capture_output=True, text=True,
                                    check=True).stdout
            hwinfo += "\n\nUSB Devices: \n\n"
            hwinfo += subprocess.run(["lsusb"], shell=False, capture_output=True, text=True, check=True).stdout
            hwinfo += "\n\nStorage Devices: \n\n"
            hwinfo += subprocess.run(["lsblk"], shell=False, capture_output=True, text=True, check=True).stdout
            textToShow += "Hardware Info:\n\n" + hwinfo + "\n\n"

            self.dialog.gauge_update(60, "Getting Disk Usage info...", True)
            diskInfo = subprocess.run(["df", "-H"], shell=False, capture_output=True, text=True, check=True).stdout
            textToShow += "Disk Usage Info:\n\n" + diskInfo + "\n\n"

            self.dialog.gauge_update(80, "Getting Memory Usage info...", True)
            memInfo = subprocess.run(["free", "-h"], shell=False, capture_output=True, text=True, check=True).stdout
            textToShow += "Memory Info:\n\n" + memInfo + "\n\n"

        except subprocess.CalledProcessError:
            logging.error("Failed to call necessary commands.")
        self.dialog.gauge_update(100, "Done!", True)
        time.sleep(1)
        self.dialog.gauge_stop()
        logging.debug("Done compiling host info.")
        self.dialog.msgbox(textToShow, title="Host Information", height=50, width=UserInteraction.WIDE_WIDTH, tab_correct=True, trim=False,
                           cr_wrap=True)

    def showSystemStatus(self):
        logging.debug("Showing system status.")

        self.dialog.infobox("Gathering status info...")
        systemdStatus = subprocess.run(["systemctl", "list-units", "--all", "open\\x2bquarter\\x2bmaster\\x2d*"],
                                       shell=False, capture_output=True, text=True, check=True).stdout

        self.dialog.msgbox(systemdStatus, title="System Status", height=50, width=UserInteraction.WIDE_WIDTH)

    def manageInstallationMenu(self):
        logging.debug("Running Manage Installation menu.")
        while True:
            code, choice = self.dialog.menu(
                "Please choose an option:",
                title="Manage Installation Menu",
                choices=[
                    ("(1)", "SSL/HTTPS Certs (TODO) "),
                    ("(2)", "Set E-mail Settings"),
                    ("(3)", "User Administration (TODO)"),
                    ("(4)", "Plugins")
                ]
            )
            UserInteraction.clearScreen()
            logging.debug('Main menu choice: %s, code: %s', choice, code)
            if code != self.dialog.OK:
                break

            if choice == "(2)":
                self.manageEmailSettings()

        logging.debug("Done running manage install menu.")

    def manageEmailSettings(self):
        # Show current email settings. Ask if want to change
        logging.info("Entering flow for managing E-mail settings.")
        # TODO



ui = UserInteraction()
