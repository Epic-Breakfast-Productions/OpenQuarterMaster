import logging
from typing import Optional

from dialog import Dialog
from ConfigManager import *
from ContainerUtils import *
from ServiceUtils import *
from EmailUtils import *
from DataUtils import *
from PackageManagement import *
from CronUtils import *
from SnapshotUtils import *
from LogManagement import *
from InputValidators import *
from CertsUtils import *
import time
import re
import os


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
            autowidgetsize=True,

        )
        self.dialog.set_background_title(ScriptInfo.SCRIPT_TITLE)
        self.dialog.__setattr__("hfile", "oqm-station-captain-help.txt")

        self.dialog.setup_debug(
            True,
            open("dialogDebug.log", 'w'),
            always_flush=True,
            expand_file_opt=True
        )

    @staticmethod
    def clearScreen():
        os.system('clear')

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
            code, value = self.dialog.passwordbox(text + "\n\n(No input will be shown when typing)", title=title)
        else:
            code, value = self.dialog.inputbox(
                text,
                title=title,
                init=mainCM.getConfigVal(configKey, exceptOnNotPresent=False),
                width=75
            )

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

    def promptForServiceRestart(self, configChanged: bool = False):
        logging.info("Prompting user to see if they want to restart services.")
        code = self.dialog.yesno("Restart all services?", title="Restart Services?")
        if code != self.dialog.OK:
            logging.info("User chose not to restart services.")
            return
        self.dialog.infobox("Restarting services. Please wait.")

        result = ServiceUtils.doServiceCommand(
            ServiceStateCommand.restart,
            ServiceUtils.SERVICE_ALL
        )

        if not result:
            self.dialog.msgbox("Service restart FAILED.\nPlease check logs for reasoning.")
            return

        self.dialog.msgbox("Service restart complete.")

    def startUserInteraction(self):
        self.userInteractionSetupCheck()
        self.mainMenu()

    def userInteractionSetupCheck(self):
        logging.info("Checking system is setup")
        if not PackageManagement.coreInstalled():
            logging.info("Core setup not installed")
            code = self.dialog.yesno("Core components are not installed. Install now?", title="Setup")
            if code == self.dialog.OK:
                self.dialog.infobox("Installing core components. Please Wait. This can take a few moments.")
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
                    ("(4)", "Cleanup, Maintenance, and Updates"),
                    ("(5)", "Captain Settings"),
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
            if choice == "(3)":
                self.snapshotsMenu()
            if choice == "(4)":
                self.cleanMaintUpdatesMenu()

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

        self.dialog.infobox("Gathering system information. please wait.")
        textToShow = ""
        try:
            textToShow += LogManagement.getSystemInfo()
        except subprocess.CalledProcessError:
            logging.error("Failed to call necessary commands.")
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
                    ("(1)", "SSL/HTTPS Certs"),
                    ("(2)", "Set E-mail Settings"),
                    ("(3)", "User Administration"),
                    ("(4)", "Plugins")
                ]
            )
            UserInteraction.clearScreen()
            logging.debug('Main menu choice: %s, code: %s', choice, code)
            if code != self.dialog.OK:
                break
            if choice == "(1)":
                self.manageCertsMenu()
            if choice == "(2)":
                self.manageEmailSettings()
            if choice == "(3)":
                self.userAdminMenu()

        logging.debug("Done running manage install menu.")

    def manageCertsMenu(self):
        logging.debug("Running Manage Certs menu.")
        certMode = mainCM.getConfigVal("cert.mode")

        choices = [
            ("(1)", "Current Cert Info"),
            ("(2)", "Verify current certs (TODO)"),
            ("(3)", f"Cert Mode (Currently {certMode})"),
            ("(4)", "Regenerate certs"),
            ("(5)", "Private Key Location"),
            ("(6)", "Public Key/Cert Location"),
        ]

        if certMode == "self":
            logging.debug("Setting up menu for self mode")
            choices.append(("(7)", "CA Private Key Location"))
            choices.append(("(8)", "CA Public Cert/Key Location"))
            choices.append(("(9)", f"Cert Country Name ({mainCM.getConfigVal('cert.selfMode.certInfo.countryName')})"))
            choices.append(("(10)", f"Cert State or Province Name ({mainCM.getConfigVal('cert.selfMode.certInfo.stateOrProvinceName')})"))
            choices.append(("(11)", f"Cert Locality Name ({mainCM.getConfigVal('cert.selfMode.certInfo.localityName')})"))
            choices.append(("(12)", f"Cert Organization Name ({mainCM.getConfigVal('cert.selfMode.certInfo.organizationName')})"))
            choices.append(("(13)", f"Cert Organizational Unit Name ({mainCM.getConfigVal('cert.selfMode.certInfo.organizationalUnitName')})"))
        if certMode == "letsEncrypt":
            logging.debug("Setting up menu for let's encrypt mode")
            accepted = mainCM.getConfigVal('cert.letsEncryptMode.acceptTerms')
            if accepted:
                accepted = "accepted"
            else:
                accepted = "NOT accepted"
            choices.append(("(14)", f"Accept Let's Encrypt's Terms of Use ({accepted})"))
        if certMode == "provided":
            logging.debug("Setting up menu for provided mode")
            choices.append(("(7)", "CA Private Key Location"))
            choices.append(("(8)", "CA Public Cert/Key Location"))
            choices.append(("(15)", f"Provide CA Cert (Currently {mainCM.getConfigVal('cert.providedMode.caProvided')})"))

        while True:
            code, choice = self.dialog.menu(
                "Please choose an option:",
                title="Manage Certs Menu",
                choices=choices
            )
            UserInteraction.clearScreen()
            logging.debug('Main menu choice: %s, code: %s', choice, code)
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                logging.debug("Showing current cert information")
                certInfoReturn = subprocess.run(["openssl", "x509", "-in", mainCM.getConfigVal('cert.certs.systemCert'), "--text"], shell=False, capture_output=True, text=True, check=False)
                self.dialog.scrollbox(certInfoReturn.stdout, title="System Cert Info")

                if mainCM.getConfigVal("cert.mode") == "self" or (mainCM.getConfigVal("cert.mode") == "provided" and mainCM.getConfigVal("cert.providedMode.caProvided")):
                    certInfoReturn = subprocess.run(["openssl", "x509", "-in", mainCM.getConfigVal('cert.certs.CARootCert'), "--text"], shell=False, capture_output=True, text=True, check=False)
                    self.dialog.scrollbox(certInfoReturn.stdout, title="CA Cert Info")

            if choice == "(2)":
                logging.debug("Verifying current cert setup (TODO)")
                # TODO
                # TODO:: in self/provided mode where applicable: openssl verify -verbose -CAfile /etc/oqm/certs/CArootCert.crt /etc/oqm/certs/systemCert.crt
            if choice == "(3)":
                logging.debug("Setting cert mode")
                self.promptForConfigChange(
                    "The method the system will use to get certs ('self', 'letsEncrypt', or 'provided'):",
                    "Set Cert Mode",
                    "cert.mode",
                    validators=[InputValidators.isCertMode]
                )
            if choice == "(4)":
                logging.debug("Regenerating Certs")
                forceCaRegen = False
                if mainCM.getConfigVal("cert.mode") == "self":
                    self.dialog.yesno("Regenerate root CA (not recommended)?")
                    if code == self.dialog.OK:
                        logging.info("User chose to regenerate root CA.")
                        forceCaRegen = True
                result, message = CertsUtils.regenCerts(forceCaRegen)
                header = "Cert Regeneration Complete"
                if not result:
                    header = "Cert Regeneration FAILED"
                self.dialog.msgbox(message, title=header)
            if choice == "(5)":
                logging.debug("Setting private key location")
                self.promptForConfigChange(
                    "The location of the system's private key:",
                    "Set Private Key Location",
                    "cert.certs.privateKey",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(6)":
                logging.debug("Setting public cert location")
                self.promptForConfigChange(
                    "The location of the system's public cert/key:",
                    "Set Public Cert/Key Location",
                    "cert.certs.systemCert",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(7)":
                logging.debug("Setting CA private key location")
                self.promptForConfigChange(
                    "The location of the system's CA private key:",
                    "Set CA Private Key Location",
                    "cert.certs.CARootPrivateKey",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(8)":
                logging.debug("Setting CA public cert location")
                self.promptForConfigChange(
                    "The location of the system's CA public cert/key:",
                    "Set CA Public Cert/Key Location",
                    "cert.certs.CARootCert",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(9)":
                logging.debug("Setting country name for self-signed cert")
                self.promptForConfigChange(
                    "The country name for self-signed certs:",
                    "Set country name for self-signed certs",
                    "cert.selfMode.certInfo.countryName",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(10)":
                logging.debug("Setting state or province name for self-signed cert")
                self.promptForConfigChange(
                    "The state or province name for self-signed certs:",
                    "Set state or province name for self-signed certs",
                    "cert.selfMode.certInfo.stateOrProvinceName",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(11)":
                logging.debug("Setting locality name for self-signed cert")
                self.promptForConfigChange(
                    "The locality name for self-signed certs:",
                    "Set locality name for self-signed certs",
                    "cert.selfMode.certInfo.localityName",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(12)":
                logging.debug("Setting organization name for self-signed cert")
                self.promptForConfigChange(
                    "The organization name for self-signed certs:",
                    "Set organization name for self-signed certs",
                    "cert.selfMode.certInfo.organizationName",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(13)":
                logging.debug("Setting organizational unit name for self-signed cert")
                self.promptForConfigChange(
                    "The organizational unit name for self-signed certs:",
                    "Set organizational unit name for self-signed certs",
                    "cert.selfMode.certInfo.organizationalUnitName",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(14)":
                logging.debug("Setting that the user has accepted Let's Encrypt's terms of use")
                mainCM.setConfigValInFile("cert.letsEncryptMode", True, ScriptInfo.CONFIG_DEFAULT_UPDATE_FILE)
                mainCM.rereadConfigData()
            if choice == "(15)":
                logging.debug("Setting if the CA was also provided.")
                code = self.dialog.yesno("Are you providing your own CA file?")
                caProvided = False
                if code == self.dialog.OK:
                    caProvided = True
                mainCM.setConfigValInFile("cert.providedMode.caProvided", caProvided, ScriptInfo.CONFIG_DEFAULT_UPDATE_FILE)
                mainCM.rereadConfigData()

        logging.debug("Done running manage certs menu.")

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
                    validators=[InputValidators.isEmail]
                )
            if choice == "(2)":
                self.promptForConfigChange(
                    "The email used as a \"from\" address on sent emails:",
                    "Set Send From Email",
                    "system.email.addressFrom",
                    validators=[InputValidators.isEmail]
                )
            if choice == "(3)":
                self.promptForConfigChange(
                    "The host of the SMTP Email Server:",
                    "SMTP Host",
                    "system.email.smtpHost",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(4)":
                self.promptForConfigChange(
                    "The port of the SMTP Email Server:",
                    "SMTP Port",
                    "system.email.smtpPort",
                    validators=[InputValidators.isDigit]
                )
            if choice == "(5)":
                self.promptForConfigChange(
                    "The username to connect to the email server with:",
                    "SMTP Username",
                    "system.email.username",
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(6)":
                self.promptForConfigChange(
                    "The port of the SMTP Email Server:",
                    "SMTP Host",
                    "system.email.password",
                    secret=True,
                    validators=[InputValidators.isNotEmpty]
                )
            if choice == "(7)":
                if EmailUtils.testEmailSettings():
                    self.dialog.msgbox("Test email sent to alert address.\nPlease check your email.")
                else:
                    self.dialog.msgbox("FAILED to send test message.\nPlease check settings and try again.")

        logging.debug("Done running manage email settings menu.")
        self.promptForServiceRestart(True)

    def userAdminMenu(self):
        logging.debug("Running User Admin menu.")
        while True:
            code, choice = self.dialog.menu(
                "System Users are managed via Keycloak.\nPlease choose an option:",
                title="User Admin Menu",
                choices=[
                    ("(1)", "Go to Keycloak"),
                    # ("(2)", "Set E-mail Settings")
                ]
            )
            UserInteraction.clearScreen()
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                self.dialog.msgbox(
                    "Keycloak access information:\n\n" +
                    "\tURL: https://" + mainCM.getConfigVal("system.hostname") + ":" + mainCM.getConfigVal(
                        "infra.keycloak.port") + "/admin/master/console/#/oqm\n" +
                    "\tAdmin user: " + mainCM.getConfigVal("infra.keycloak.adminUser") + "\n" +
                    "\tAdmin Password: " + mainCM.getConfigVal("infra.keycloak.adminPass"),
                    title="Keycloak Access",
                    width=UserInteraction.WIDE_WIDTH
                )

        logging.debug("Done running user admin menu.")

    def cleanMaintUpdatesMenu(self):
        logging.debug("Running Cleanup, Maintenance, and Updates menu.")
        while True:
            code, choice = self.dialog.menu(
                "Please choose an option:",
                title="Cleanup, Maintenance, and Updates",
                choices=[
                    ("(1)", "Updates"),
                    ("(2)", "Containers"),
                    ("(3)", "Data"),
                    ("(4)", "Package logs"),
                    ("(5)", "Restart Services"),
                    ("(6)", "Restart Device"),
                ]
            )
            UserInteraction.clearScreen()
            logging.debug('Main menu choice: %s, code: %s', choice, code)
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                self.updatesManagementMenu()
            if choice == "(2)":
                self.containerManagementMenu()
            if choice == "(3)":
                self.dataManagementMenu()
            if choice == "(4)":
                self.dialog.infobox("Packaging logs. Please wait.")
                LogManagement.packageLogs()
            if choice == "(5)":
                self.promptForServiceRestart()
            if choice == "(6)":
                code = self.dialog.yesno("Restart device?", title="Restart Device?")
                if code == self.dialog.OK:
                    logging.info("User chose to reboot the device.")
                    self.dialog.infobox("Rebooting device. Please wait.")
                    os.system('reboot')

        logging.debug("Done Cleanup, Maintenance, and Updates menu.")

    def updatesManagementMenu(self):
        logging.debug("Running Updates menu.")
        while True:
            code, choice = self.dialog.menu(
                "Manage the system's updates:",
                title="Updates Management Menu",
                choices=[
                    ("(1)", "Run updates now"),
                    ("(2)", "Enable/Disable automatic updates (recommend enabled)"),
                ]
            )
            UserInteraction.clearScreen()
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                self.dialog.infobox("Running updates. Please wait.")
                result, output = PackageManagement.updateSystem()

                if not result:
                    self.dialog.msgbox("Failed to run updates:\n\n" + output, title="Updates Failed")
                    continue

                code = self.dialog.yesno("Updates complete. Restart?", title="Restart?")
                if code == self.dialog.OK:
                    logging.info("User chose to restart after updates.")
                    os.system('reboot')
                else:
                    logging.info("User chose not to restart after updates.")

            if choice == "(2)":
                PackageManagement.promptForAutoUpdates()
        logging.debug("Done running container management menu.")

    def snapshotsMenu(self):
        logging.debug("Running snapshots menu.")
        while True:
            code, choice = self.dialog.menu(
                "Please choose an option:",
                title="Snapshots",
                choices=[
                    ("(1)", "Restore from Snapshot"),
                    ("(2)", "Perform Snapshot Now"),
                    ("(3)", ("Disable" if SnapshotUtils.isAutomaticEnabled() else "Enable") + " automatic snapshots"),
                    ("(4)", "Set snapshot location"),
                    ("(5)", "Set number of snapshots to keep"),
                    ("(6)", "Set auto snapshot frequency"),
                ]
            )
            UserInteraction.clearScreen()
            logging.debug('Menu choice: %s, code: %s', choice, code)
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                logging.info("Choosing snapshot file to restore from")

                # /usr/bin/dialog --backtitle 'Open QuarterMaster Station Captain VSCRIPT_VERSION' --title 'Choose Snapshot file to restore from.' --fselect '/data/oqm-snapshots/*' 50 200
                code, snapshotFile = self.dialog.fselect(
                    mainCM.getConfigVal("snapshots.location") + "/*",
                    title="Choose Snapshot file to restore from.",
                    width=100,
                    height=15
                )

                if code != self.dialog.OK:
                    logging.info("User chose not to restore snapshot after all.")
                else:
                    logging.info("User chose snapshot file %s", snapshotFile)
                    if not os.path.exists(snapshotFile):
                        logging.error("File chosen by user does not exist.")
                        self.dialog.msgbox("File chosen does not exist.")
                        continue
                    if not os.path.isfile(snapshotFile):
                        logging.error("File chosen by user not a file.")
                        self.dialog.msgbox("File chosen was not a file.")
                        continue
                    if not snapshotFile.endswith((".tar.gz", ".tar.xz", ".tar.bz")):
                        logging.error("File chosen by user not a valid type.")
                        self.dialog.msgbox("File chosen was not a supported type.")
                        continue

                    code = self.dialog.yesno("Take a preemptive snapshot, just in case?")
                    if code == self.dialog.OK:
                        logging.info("User chose to take a preemptive snapshot.")
                        SnapshotUtils.performSnapshot(SnapshotTrigger.preemptive)
                    else:
                        logging.info("User chose not to take a preemptive snapshot.")

                    code = self.dialog.yesno("Are you want to restore the following snapshot?\n" + snapshotFile + "\n\nThis can't be undone.")
                    if code != self.dialog.OK:
                        logging.info("User chose not to do the restore after all.")
                        continue

                    SnapshotUtils.restoreFromSnapshot(snapshotFile)
            if choice == "(2)":
                self.dialog.infobox("Performing snapshot. Please wait.")
                result, report = SnapshotUtils.performSnapshot(SnapshotTrigger.manual)
                if not result:
                    self.dialog.msgbox(report, title="Error taking Snapshot")
                else:
                    self.dialog.msgbox("Snapshot was taken successfully.\n\nOutput File:\n" + report, title="Snapshot successful")
            if choice == "(3)":
                if SnapshotUtils.isAutomaticEnabled():
                    SnapshotUtils.disableAutomatic()
                    self.dialog.msgbox("Disabled automatic snapshots.")
                else:
                    SnapshotUtils.enableAutomatic()
                    self.dialog.msgbox("Enabled automatic snapshots.")
            if choice == "(4)":
                self.promptForConfigChange(
                    "The location that snapshots are placed:",
                    "Snapshot Location",
                    "snapshots.location",
                    validators=[InputValidators.isWritableDirectory]
                )
            if choice == "(5)":
                self.promptForConfigChange(
                    "The number of snapshots to keep:",
                    "Number of snapshots",
                    "snapshots.numToKeep",
                    validators=[InputValidators.isDigit]
                )
            if choice == "(6)":
                self.promptForConfigChange(
                    "The frequency that snapshots are taken:\n\n(Options: " + CronFrequency.getFreqListStr() + ")",
                    "Auto Snapshot Frequency",
                    "snapshots.frequency",
                    validators=[InputValidators.isCronKeyword]
                )
                if SnapshotUtils.isAutomaticEnabled():
                    SnapshotUtils.enableAutomatic()

        logging.debug("Done snapshots menu.")

    def dataManagementMenu(self):
        logging.debug("Running data management menu.")
        while True:
            code, choice = self.dialog.menu(
                "Manage the system's data:",
                title="Data Management Menu",
                choices=[
                    ("(1)", "Clear ALL Data"),
                    # TODO
                    # ("(2)", "Clear OQM Data"),
                    # ("(2)", "Clear User Data"),
                    # ("(2)", "Clear Plugin Data"),
                ]
            )
            UserInteraction.clearScreen()
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                code = self.dialog.yesno(
                    "This will remove ALL data, including users.\nAre you sure?",
                    title="Confirm"
                )
                if code != self.dialog.OK:
                    logging.info("User chose not to clear data.")
                else:
                    self.dialog.infobox("Clearing ALL data. Please wait.")
                    if DataUtils.clearAllData():
                        self.dialog.msgbox("Data was cleared.")
                    else:
                        self.dialog.msgbox("Clearing of data FAILED.")

        logging.debug("Done running data management menu.")

    def containerManagementMenu(self):
        logging.debug("Running container management menu.")
        while True:
            code, choice = self.dialog.menu(
                "Manage the system's containers:",
                title="Container Management Menu",
                choices=[
                    ("(1)", "Prune unused container resources"),
                    ("(2)",
                     ("Disable" if ContainerUtils.isAutomaticEnabled() else "Enable") + " automatic pruning (recommend enabled)"),
                    ("(3)", "Set prune frequency"),
                ]
            )
            UserInteraction.clearScreen()
            if code != self.dialog.OK:
                break

            if choice == "(1)":
                self.dialog.infobox("Pruning container resources. Please wait.")
                freed = ContainerUtils.pruneContainerResources()
                self.dialog.msgbox("Finished pruning container resources.\n\nFreed " + freed)
            if choice == "(2)":
                if ContainerUtils.isAutomaticEnabled():
                    ContainerUtils.disableAutomatic()
                    self.dialog.msgbox("Disabled automatic pruning of container resources.")
                else:
                    ContainerUtils.enableAutomatic()
                    self.dialog.msgbox("Enabled automatic pruning of container resources.")
            if choice == "(3)":
                self.promptForConfigChange(
                    "The frequency that pruning of container resources is performed:\n\n(Options: " + CronFrequency.getFreqListStr() + ")\n(Recommend Monthly)",
                    "Auto Pruning Frequency",
                    "system.automaticContainerPruneFrequency",
                    validators=[InputValidators.isCronKeyword]
                )
                if ContainerUtils.isAutomaticEnabled():
                    ContainerUtils.enableAutomatic()

        logging.debug("Done running container management menu.")


ui = UserInteraction()
