import ipaddress
import shutil
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives._serialization import BestAvailableEncryption
from cryptography.hazmat.primitives.asymmetric.rsa import RSAPrivateKey
from cryptography.hazmat.primitives.serialization import load_pem_private_key, pkcs12
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import hashes
import subprocess
import os
import datetime
from cryptography import x509
from cryptography.x509 import Certificate, Extensions, SubjectAlternativeName, GeneralName, ExtensionOID
from future.backports.datetime import timedelta

from ConfigManager import *
from CronUtils import *
from ServiceUtils import *
from LogUtils import *
from ipaddress import *

from PackageManagement import PackageManagement


class CertsUtils:
    """
    Resources:
     - https://cryptography.io/en/latest/x509/reference/
     - https://stackoverflow.com/questions/54677841/how-do-can-i-generate-a-pkcs12-file-using-python-and-the-cryptography-module
    """
    log = LogUtils.setupLogger("CertsUtils")
    AUTO_REGEN_CERTS_CRON_NAME = "autoRegenCerts"

    @staticmethod
    def newPrivateKey() -> RSAPrivateKey:
        """
        Gets a new default private key setup
        :return: A new private key
        """
        return rsa.generate_private_key(
            public_exponent=65537,
            key_size=2048,
            backend=default_backend()
        )

    @staticmethod
    def getSAN(san: str) -> GeneralName:
        """
        Gets a new SAN (Subject Alternative Name) entry for the string given.

        If IP, returns and IP name. Otherwise, DNS
        :param san:
        :return:
        """
        try:
            return x509.IPAddress(ipaddress.ip_address(san))
        except ValueError:
            return x509.DNSName(san)

    @staticmethod
    def certExpiresSoon(cert: Certificate) -> bool:
        """
        Determines if a cert is expiring soon based on the configured value.
        :param cert:
        :return: If the cert expires soon
        """
        expiring = cert.not_valid_after
        now = datetime.datetime.now(datetime.timezone.utc)
        soonThreshold = expiring - timedelta(days=mainCM.getConfigVal("cert.selfSigned.daysBeforeExpiryToRegen"))
        return now >= soonThreshold

    @staticmethod
    def ensureCaInstalled(force: bool = False) -> (bool, str):
        """
        Ensures that the CA is installed on this system.

        Installs to:
          - /usr/local/share/ca-certificates/ (and subsequently calls update-ca-certificates)
          - Wherever firefox keeps its system certs
        :param force:
        :return:
        """
        output = ""
        root_ca_cert_path = mainCM.getConfigVal("cert.certs.CARootCert")
        caCertName = os.path.basename(root_ca_cert_path)

        # Install in system location
        updatePrevious = False
        if force:
            updatePrevious = True

        if os.path.exists("/usr/local/share/ca-certificates/" + caCertName):
            updatePrevious = True
            os.remove("/usr/local/share/ca-certificates/" + caCertName)

        if force or updatePrevious:
            output += f"Removed previously installed root CA from system: {caCertName}\n\n"
            result = subprocess.run(["update-ca-certificates"], shell=False, capture_output=True, text=True, check=True)

        shutil.copy(root_ca_cert_path, "/usr/local/share/ca-certificates/")
        result = subprocess.run(["update-ca-certificates"], shell=False, capture_output=True, text=True, check=True)
        output += "Output from updating system ca certs:\n" + result.stdout + "\n\n"

        # Install for Firefox
        #   Where FF is a snap, no /usr/lib/firefox or /usr/lib/mozilla
        ffInstalled = False
        ffPoliciesFile = None
        ffPoliciesDir = None
        ffCertsDir = None
        if os.path.exists("/usr/lib/firefox"):
            CertsUtils.log.debug("Firefox installed directly.")
            ffInstalled = True
            ffPoliciesFile = "/usr/lib/firefox/distribution/policies.json"
            ffPoliciesDir = os.path.basename(ffPoliciesFile)
            ffCertsDir = "/usr/lib/mozilla/certificates/"
        elif os.path.exists("/etc/firefox"):
            CertsUtils.log.debug("Firefox probably installed via snap.")
            ffInstalled = True
            ffPoliciesFile = "/etc/firefox/policies/policies.json"
            ffPoliciesDir = os.path.basename(ffPoliciesFile)
            ffCertsDir = "/etc/firefox/policies/certificates/"
        elif PackageManagement.checkFirefoxSnapInstalled():
            CertsUtils.log.debug("Firefox probably installed via newer snap.")
            ffInstalled = True
            ffPoliciesFile = "/etc/firefox/policies/policies.json"
            ffPoliciesDir = os.path.basename(ffPoliciesFile)
            ffCertsDir = "/etc/firefox/policies/certificates/"
        else:
            CertsUtils.log.debug("Firefox not found on system.")

        if ffInstalled:
            CertsUtils.log.info("Firefox installed, setting up firefox certs.")

            Path(ffCertsDir).mkdir(parents=True, exist_ok=True)
            shutil.copy(root_ca_cert_path, ffCertsDir)

            # update Firefox policies
            #
            policiesJson = None
            if not os.path.exists(ffPoliciesFile):
                Path(ffPoliciesDir).mkdir(parents=True, exist_ok=True)
                policiesJson = {
                    "policies": {
                        "ImportEnterpriseRoots": True,
                        "Certificates": {
                            "Install": [
                                f"{ffCertsDir}{caCertName}"
                            ]
                        }
                    }
                }
            else:
                # Read in and update policies
                with open(ffPoliciesFile, 'r') as stream:
                    policiesJson = json.load(stream)
                policiesJson["policies"]["ImportEnterpriseRoots"] = True
                certInstallList = policiesJson["policies"]["Certificates"]["Install"]
                if caCertName not in certInstallList:
                    certInstallList.append(caCertName)
            with open(ffPoliciesFile, 'w') as stream:
                json.dump(policiesJson, stream)
            output += "Setup CA in Firefox.\n\n"
        else:
            output += "Firefox not installed.\n\n"

        return True, output

    @staticmethod
    def enableAutoRegenCerts():
        """
        Enables the cert regen checks to automatically happen through a cron job, monthly
        :return:
        """
        CronUtils.enableCron(
            CertsUtils.AUTO_REGEN_CERTS_CRON_NAME,
            "oqm-captain --regen-certs",
            CronFrequency.monthly
        )

    @staticmethod
    def disableAutoRegenCerts():
        CronUtils.disableCron(CertsUtils.AUTO_REGEN_CERTS_CRON_NAME)

    @staticmethod
    def isAutoRegenCertsEnabled() -> bool:
        return CronUtils.isCronEnabled(CertsUtils.AUTO_REGEN_CERTS_CRON_NAME)

    @staticmethod
    def generateRootCA() -> (bool, str):
        """
        Generates a new CA, then installs it on the system.
        :return:
        """
        CertsUtils.log.info("Generating a new root CA")
        root_ca_key_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootPrivateKey")
        root_ca_cert_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootCert")
        ca_private_key = CertsUtils.newPrivateKey()

        ca_name = x509.Name([
            x509.NameAttribute(x509.NameOID.COUNTRY_NAME,           mainCM.getConfigVal("cert.selfSigned.certInfo.countryName")),
            x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.stateOrProvinceName")),
            x509.NameAttribute(x509.NameOID.LOCALITY_NAME,          mainCM.getConfigVal("cert.selfSigned.certInfo.localityName")),
            x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME,      mainCM.getConfigVal("cert.selfSigned.certInfo.organizationName")),
            x509.NameAttribute(x509.NameOID.COMMON_NAME,            mainCM.getConfigVal("cert.selfSigned.certInfo.caCommonName")),
        ])
        nvb = datetime.datetime.now(datetime.UTC)
        nva = nvb + datetime.timedelta(days=mainCM.getConfigVal("cert.selfSigned.rootCaTtl"))

        root_cert = (
            x509.CertificateBuilder()
            .subject_name(ca_name)
            .issuer_name(ca_name)
            .public_key(ca_private_key.public_key())
            .serial_number(x509.random_serial_number())
            .not_valid_before(nvb)
            .not_valid_after(nva)
            .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
        )
        root_cert = root_cert.sign(ca_private_key, hashes.SHA256(), default_backend())

        with open(root_ca_key_path, 'wb') as key_file:
            key_file.write(
                ca_private_key.private_bytes(
                    encoding=serialization.Encoding.PEM,
                    format=serialization.PrivateFormat.TraditionalOpenSSL,
                    encryption_algorithm=serialization.NoEncryption()
                )
            )

        with open(root_ca_cert_path, 'wb') as cert_file:
            cert_file.write(
                root_cert.public_bytes(
                    encoding=serialization.Encoding.PEM
                )
            )
        returned, caInstallOutput = CertsUtils.ensureCaInstalled()
        if not returned:
            return False, caInstallOutput
        return True

    @staticmethod
    def generateCert(
            keyFile,
            certFile,
            commonName,
            subjectAltNames=None,
            csrFile = None,
            keystoreFile = None,
            keystorePassword = None
    ) -> (bool, str):
        """
        Generates a cert based on the information given.
        :param keyFile:
        :param certFile:
        :param commonName:
        :param subjectAltNames:
        :param csrFile:
        :param keystoreFile:
        :param keystorePassword: Must be set if keystore file given.
        :return:
        """
        CertsUtils.log.info("Generating a new cert")

        root_ca_key_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootPrivateKey")
        root_ca_cert_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootCert")

        private_key = CertsUtils.newPrivateKey()

        name = x509.Name([
            x509.NameAttribute(x509.NameOID.COUNTRY_NAME,             mainCM.getConfigVal("cert.selfSigned.certInfo.countryName")),
            x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME,   mainCM.getConfigVal("cert.selfSigned.certInfo.stateOrProvinceName")),
            x509.NameAttribute(x509.NameOID.LOCALITY_NAME,            mainCM.getConfigVal("cert.selfSigned.certInfo.localityName")),
            x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME,        mainCM.getConfigVal("cert.selfSigned.certInfo.organizationName")),
            x509.NameAttribute(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.organizationalUnitName")),
            x509.NameAttribute(x509.NameOID.COMMON_NAME, commonName),
        ])
        nvb = datetime.datetime.now(datetime.timezone.utc)
        nva = nvb + datetime.timedelta(days=mainCM.getConfigVal("cert.selfSigned.systemCertTtl"))

        csr = (x509.CertificateSigningRequestBuilder()
               .subject_name(name)
               .sign(private_key, hashes.SHA256(), default_backend())
               )

        root_ca_cert = x509.load_pem_x509_certificate(open(root_ca_cert_path, 'rb').read(), default_backend())
        root_ca_key = serialization.load_pem_private_key(open(root_ca_key_path, 'rb').read(), password=None, backend=default_backend())

        certBuilder = (x509.CertificateBuilder()
                       .subject_name(name)
                       .issuer_name(root_ca_cert.subject)
                       .serial_number(x509.random_serial_number())
                       .not_valid_before(nvb)
                       .not_valid_after(nva)
                       .public_key(csr.public_key())
                       )
        cnNameInSANS = False
        if subjectAltNames is not None:
            for curSanStr in subjectAltNames:
                if curSanStr == commonName:
                    cnNameInSANS = True
                certBuilder = certBuilder.add_extension(x509.SubjectAlternativeName([CertsUtils.getSAN(curSanStr)]), critical=False)
            if not cnNameInSANS:
                certBuilder = certBuilder.add_extension(x509.SubjectAlternativeName([CertsUtils.getSAN(commonName)]), critical=False)

        cert = certBuilder.sign(root_ca_key, hashes.SHA256(), default_backend())

        # Write out private key
        with open(keyFile, 'wb') as key_file:
            key_file.write(
                private_key.private_bytes(
                    encoding=serialization.Encoding.PEM,
                    format=serialization.PrivateFormat.TraditionalOpenSSL,
                    encryption_algorithm=serialization.NoEncryption()
                )
            )
        # Write out cert
        with open(certFile, 'wb') as cert_file:
            cert_file.write(
                cert.public_bytes(
                    encoding=serialization.Encoding.PEM
                )
            )

        # Generate Keystore
        if keystoreFile is not None:
            with (open(keystoreFile, 'wb') as cert_file,
                  open(root_ca_cert_path, "rb") as root_ca_cert,
                  open(root_ca_key_path, "rb") as root_ca_key
                  ):
                cert = x509.load_pem_x509_certificate(root_ca_cert.read())
                key = load_pem_private_key(root_ca_key.read(), None)

                cert_file.write(
                    pkcs12.serialize_key_and_certificates(
                        b"OQM CA",
                        key,
                        cert,
                        None,
                        BestAvailableEncryption(bytes(keystorePassword, 'UTF-8'))
                    )
                )
        # write out CSR
        if csrFile is not None:
            with open(csrFile, 'wb') as csr_file:
                csr_file.write(
                    csr.public_bytes(
                        encoding=serialization.Encoding.PEM
                    )
                )
        CertsUtils.log.info("Finished writing new cert.")
        return True

    @staticmethod
    def generateSystemCert() -> (bool, str):
        """
        Wrapper for generateCert(), configured for writing the external system cert.
        :return:
        """
        return CertsUtils.generateCert(
            mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCertKey"),
            mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCert"),
            mainCM.getConfigVal("system.hostname"),
            mainCM.getConfigVal("cert.selfSigned.certInfo.additionalExternalSANs"),
            mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCertCsr"),
            mainCM.getConfigVal("cert.selfSigned.certInfo.systemExternalKeystore") if mainCM.getConfigVal("cert.selfSigned.generateKeystore") else None,
            mainCM.getConfigVal("cert.selfSigned.certInfo.systemExternalKeystorePass") if mainCM.getConfigVal("cert.selfSigned.generateKeystore") else None
        )

    @staticmethod
    def ensureRootCA(force=False) -> (bool, str, bool):
        """
        Ensures that the root CA is present, and not expiring soon.
        :param force: If to force writing a new CA anyways
        :return: success, message, if new certs written
        """
        CertsUtils.log.info("Ensuring self signed root ca is present")
        root_ca_key_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootPrivateKey")
        root_ca_cert_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootCert")

        needWritten = False
        if force or not all(os.path.exists(path) for path in [root_ca_key_path, root_ca_cert_path]):
            CertsUtils.log.info("Root CA did NOT exist.")
            needWritten = True
        else:
            rootCert = x509.load_pem_x509_certificate(open(root_ca_cert_path, 'rb').read(), default_backend())
            if CertsUtils.certExpiresSoon(rootCert):
                CertsUtils.log.info("Root CA expiring soon.")
                needWritten = True

        if needWritten:
            CertsUtils.log.info("Writing new CA.")
            success, message = CertsUtils.generateRootCA()
            return success, message, True

        return True, "Root CA Already Existent and not expiring soon.", False

    @staticmethod
    def ensureSystemCerts(force=False) -> (bool, str, bool):
        """
        Ensures that the external system cert is present, and not expiring soon.
        :param force: If to force writing a new CA anyways
        :return: success, message, if new certs written
        """
        CertsUtils.log.info("Ensuring self signed system cert is present")
        systemCertKeyPath = mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCertKey")
        systemCertPath = mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCert")

        needWritten = False
        if force or not all(os.path.exists(path) for path in [systemCertKeyPath, systemCertPath]):
            CertsUtils.log.info("System Cert did NOT exist.")
            needWritten = True
        else:
            rootCert = x509.load_pem_x509_certificate(open(systemCertPath, 'rb').read(), default_backend())
            if CertsUtils.certExpiresSoon(rootCert):
                CertsUtils.log.info("System Cert expiring soon.")
                needWritten = True

        if needWritten:
            CertsUtils.log.info("Writing new System Cert.")
            success, message = CertsUtils.generateSystemCert()
            return success, message, True

        return True, "System Cert Already Existent and not expiring soon.", False

    @staticmethod
    def ensureCoreCerts(force=False) -> (bool, str, bool):
        """
        Ensures all system level certs are where they need to be. Wrapper for both ensureRootCA() and ensureSystemCerts()
        :param force: If to write these certs anyways.
        :return: success, message, if new certs written
        """
        CertsUtils.log.info("Ensuring core certs (CA and system certs) exist.")
        caSuccess, caMessage, caWritten = CertsUtils.ensureRootCA(force)
        sysSuccess, sysMessage, sysWritten = CertsUtils.ensureSystemCerts(force)
        return (caSuccess and sysSuccess), caMessage + " " + sysMessage, (caWritten and sysWritten)

    @staticmethod
    def generateInternalCert(host, destination) -> (bool, str):
        """
        Wrapper for generateCert(), configured for writing a cert for an internal service to use.
        :return:
        """
        CertsUtils.log.info("Writing new cert for internal service %s", host)
        return CertsUtils.generateCert(
            destination + "/serviceCertKey.pem",
            destination + "/serviceCert.crt",
            host,
            None,
            None,
            destination + "/serviceCertKeystore.p12",
            mainCM.getConfigVal("cert.selfSigned.certInfo.internalKeystorePass")
        )
