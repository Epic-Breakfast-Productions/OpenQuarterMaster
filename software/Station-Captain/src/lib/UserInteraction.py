import logging
from typing import Optional

from dialog import Dialog
from ConfigManager import *
from EmailUtils import *
from PackageManagement import *
import time
import re



class UserInteraction:
    """

    User interaction functions.

    References:
        - https://pythondialog.sourceforge.io/doc/
    """
    WIDE_WIDTH = 200
    TALL_HEIGHT = 50

    def __init__(self):
        self.dialog = Dialog(
            dialog="dialog",
            # autowidgetsize=True
        )
        self.dialog.set_background_title(ScriptInfo.SCRIPT_TITLE)

    @staticmethod
    def clearScreen():
        os.system('clear')

    @staticmethod
    def configValidatorEmail(val: str) -> Optional[str]:
        # I know this looks dumb, but fullmatch returns an obj and not a straight bool so this makes the linter happy
        if re.fullmatch(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,7}\b', val):
            return None
        return "Was not an email."

    @staticmethod
    def configValidatorNotEmpty(val: str) -> Optional[str]:
        if not bool(val and val.strip()):
            return "Value given was blank"
        return None

    @staticmethod
    def configValidatorIsDigit(val: str) -> Optional[str]:
        if not val.isdigit():
            return "Value given was not a number"
        return None

    def promptForConfigChange(self,
                              text: str,
                              title: str,
                              configKey: str,
                              secret: bool = False,
                              validators: list = []
                              ):
        """
        Prompts the user for a particular config value, and sets it
        :param validators:
        :param text: The text to display to the user
        :param title: The title of the message box
        :param configKey: The config key we are tweaking
        :param secret: If the config to set is a secret or not
        :return: None
        """
        logging.info("Prompting to change config key " + configKey)

        if secret:
            code, value = self.dialog.passwordbox(text, title=title)
        else:
            code, value = self.dialog.inputbox(text + "\n\n(No input will be shown when typing)", title=title,
                                               init=mainCM.getConfigVal(configKey, exceptOnNotPresent=False))

        if code != self.dialog.OK:
            self.dialog.msgbox("Canceled Setting value.")
            return

        for validator in validators:
            validationErr = validator(value)
            if validationErr is not None:
                logging.warning("Got validation error from value given from user.")
                self.dialog.msgbox("Invalid value given. Error: \n\t" + validationErr)
                return

        try:
            if secret:
                mainCM.setSecretValInFile(configKey, value, ScriptInfo.CONFIG_DEFAULT_UPDATE_FILE)
            else:
                mainCM.setConfigValInFile(configKey, value, ScriptInfo.CONFIG_DEFAULT_UPDATE_FILE)
        except Exception:
            logging.error("FAILED to set config value.")
            self.dialog.msgbox("FAILED Setting value. Please try again.")
            return

        mainCM.rereadConfigData()
        self.dialog.msgbox("Set new value")

        logging.info("Done prompting to change config key " + configKey)

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
        self.dialog.scrollbox(textToShow, title="Host Information",
                              #    height=UserInteraction.TALL_HEIGHT,
                              # width=UserInteraction.WIDE_WIDTH,
                              #    tab_correct=True, trim=False,
                              # cr_wrap=True
                              )

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
        logging.info("Entering flow for managing E-mail settings.")
        # TODO:: add option to test email settings: https://docs.python.org/3.8/library/email.examples.html
        # Show current email settings. Ask if want to change
        while True:
            code, choice = self.dialog.menu(
                "Current Email Settings:\n" +
                "\tSystem Alert Destination: " + mainCM.getConfigVal("system.email.sysAlertDest", processSecret=False,
                                                      exceptOnNotPresent=False) + "\n" +
                "\tSend From: " + mainCM.getConfigVal("system.email.addressFrom", processSecret=False,
                                                      exceptOnNotPresent=False) + "\n" +
                "\tSMTP Host: " + mainCM.getConfigVal("system.email.smtpHost", processSecret=False,
                                                      exceptOnNotPresent=False) + "\n" +
                "\tSMTP Port: " + mainCM.getConfigVal("system.email.smtpPort", processSecret=False,
                                                      exceptOnNotPresent=False) + "\n" +
                "\tUsername: " + mainCM.getConfigVal("system.email.username", processSecret=False,
                                                     exceptOnNotPresent=False) + "\n" +
                "\tPassword: " + mainCM.getConfigVal("system.email.password", processSecret=False,
                                                     exceptOnNotPresent=False) + "\n" +
                "\n\nChoose a config to change:",
                title="Manage Installation Menu",
                choices=[
                    ("(1)", "System Alert Destination"),
                    ("(2)", "Send from"),
                    ("(3)", "SMTP Host"),
                    ("(4)", "SMTP Port"),
                    ("(5)", "Username"),
                    ("(6)", "Password"),
                    ("(7)", "Test Email Settings")
                ],
                height=UserInteraction.TALL_HEIGHT
            )
            UserInteraction.clearScreen()
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                self.promptForConfigChange(
                    "The email used to send system alert emails:",
                    "Set Alert Email",
                    "system.email.sysAlertDest",
                    validators=[UserInteraction.configValidatorEmail]
                )
            if choice == "(2)":
                self.promptForConfigChange(
                    "The email used as a \"from\" address on sent emails:",
                    "Set Send From Email",
                    "system.email.addressFrom",
                    validators=[UserInteraction.configValidatorEmail]
                )
            if choice == "(3)":
                self.promptForConfigChange(
                    "The host of the SMTP Email Server:",
                    "SMTP Host",
                    "system.email.smtpHost",
                    validators=[UserInteraction.configValidatorNotEmpty]
                )
            if choice == "(4)":
                self.promptForConfigChange(
                    "The port of the SMTP Email Server:",
                    "SMTP Port",
                    "system.email.smtpPort",
                    validators=[UserInteraction.configValidatorIsDigit]
                )
            if choice == "(5)":
                self.promptForConfigChange(
                    "The username to connect to the email server with:",
                    "SMTP Username",
                    "system.email.username",
                    validators=[UserInteraction.configValidatorNotEmpty]
                )
            if choice == "(6)":
                self.promptForConfigChange(
                    "The port of the SMTP Email Server:",
                    "SMTP Host",
                    "system.email.password",
                    secret=True,
                    validators=[UserInteraction.configValidatorNotEmpty]
                )
            if choice == "(7)":
                if EmailUtils.testEmailSettings():
                    self.dialog.msgbox("Test email sent to alert address.\nPlease check your email.")
                else:
                    self.dialog.msgbox("FAILED to send test message.\nPlease check settings and try again.")

    logging.debug("Done running manage email settings menu.")


ui = UserInteraction()
