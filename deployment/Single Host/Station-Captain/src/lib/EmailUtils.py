from ConfigManager import *
import smtplib
from email.message import EmailMessage
from LogUtils import *

log = LogUtils.setupLogger(__name__)


class EmailUtils:
    """
    Class to encapsulate methods that deal with emails

    https://docs.python.org/3.8/library/email.html#module-email
    https://docs.python.org/3.8/library/email.examples.html
    https://docs.python.org/3.8/library/smtplib.html#module-smtplib

    """

    @staticmethod
    def testEmailSettings() -> bool:
        log.info("Testing system's provided email settings.")
        email = EmailMessage()
        email['Subject'] = "OQM System Email Test"
        email['From'] = mainCM.getConfigVal("system.email.addressFrom", processSecret=False, exceptOnNotPresent=False)
        email['To'] = mainCM.getConfigVal("system.email.sysAlertDest", processSecret=False, exceptOnNotPresent=False)

        try:
            s = smtplib.SMTP(
                mainCM.getConfigVal("system.email.smtpHost", exceptOnNotPresent=False),
                mainCM.getConfigVal("system.email.smtpPost", exceptOnNotPresent=False)
            )
            s.login(
                mainCM.getConfigVal("system.email.username", exceptOnNotPresent=False),
                mainCM.getConfigVal("system.email.password", exceptOnNotPresent=False)
            )
            s.send_message(email)
        except smtplib.SMTPException as e:
            log.warning("Failed to send test message.")
            return False
        return True
