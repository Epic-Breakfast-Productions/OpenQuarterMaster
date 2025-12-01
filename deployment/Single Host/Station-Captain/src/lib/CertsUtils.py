import ipaddress
import shutil
from argparse import ArgumentParser

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
from datetime import timedelta

from ConfigManager import *
from CronUtils import *
from ServiceUtils import *
from LogUtils import *
from ipaddress import *
import requests

from PackageManagement import PackageManagement


class CertsUtils:
    """
    Resources:
     - https://cryptography.io/en/latest/x509/reference/
     - https://stackoverflow.com/questions/54677841/how-do-can-i-generate-a-pkcs12-file-using-python-and-the-cryptography-module
    """
    log = LogUtils.setupLogger("CertsUtils")
    AUTO_REGEN_CERTS_CRON_NAME = "autoRegenCerts"
    LETSE_CA_SOURCES = {  # https://letsencrypt.org/certificates/
        "ISRG Root X1": ["https://letsencrypt.org/certs/isrg-root-x1-cross-signed.pem"],
        "ISRG Root X2": ["https://letsencrypt.org/certs/isrg-root-x2-cross-signed.pem"],
        "Let's Encrypt E5": ["https://letsencrypt.org/certs/2024/e5.pem", "https://letsencrypt.org/certs/2024/e5-cross.pem"],
        "Let's Encrypt E6": ["https://letsencrypt.org/certs/2024/e6.pem", "https://letsencrypt.org/certs/2024/e6-cross.pem"],
        "Let's Encrypt R10": ["https://letsencrypt.org/certs/2024/r10.pem"],
        "Let's Encrypt R11": ["https://letsencrypt.org/certs/2024/r11.pem"],
    }

    @classmethod
    def setupArgParser(cls, subparsers):
        certsParser = subparsers.add_parser("certs", help="Commands for managing certs.")
        certsSubparsers = certsParser.add_subparsers(dest="certsSubcommand")

        rc_parser = certsSubparsers.add_parser("regen", help="Regenerates the system certs based on configuration.")
        rc_parser.set_defaults(func=cls.regenCertsFromArgs)

        ecp_parser = certsSubparsers.add_parser("ensure-system-present", help="Ensures that the system certs are present and usable by the system.")
        ecp_parser.set_defaults(func=cls.ensureCertsFromArgs)

        ecp_parser = certsSubparsers.add_parser("write-internal", help="Writes certs for an internal service to use.")
        ecp_parser.add_argument(dest="service", help="The name of the service (the domain name to access the service).")
        ecp_parser.add_argument(dest="destination", help="The directory to place the new certs.")
        ecp_parser.set_defaults(func=cls.writeInternalCertsFromArgs)

    @classmethod
    def regenCertsFromArgs(cls, args):
        result, message = CertsUtils.regenCerts()
        if not result:
            print("Failed to generate certs: " + message)
            exit(4)
        print(message)

    @classmethod
    def ensureCertsFromArgs(cls, args):
        result, message, written = CertsUtils.ensureCoreCerts()
        if not result:
            print("Failed to validate certs: " + message)
            exit(5)
        print(message)

    @classmethod
    def writeInternalCertsFromArgs(cls, args):
        result, message = CertsUtils.generateInternalCert(
            args.service,
            args.destination
        )
        if not result:
            print("Failed to write certs for internal service: " + message)
            exit(6)
        print(message)

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
        now = datetime.datetime.now(datetime.timezone.utc).replace(tzinfo=None)  # expring doesn't have tz data; fails to compare otherwise
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
        root_ca_cert_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootCert")
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
            "oqm-captain certs regen",
            CronFrequency.monthly
        )

    @staticmethod
    def disableAutoRegenCerts():
        CronUtils.disableCron(CertsUtils.AUTO_REGEN_CERTS_CRON_NAME)

    @staticmethod
    def isAutoRegenCertsEnabled() -> bool:
        return CronUtils.isCronEnabled(CertsUtils.AUTO_REGEN_CERTS_CRON_NAME)

    @classmethod
    def generateSystemTruststores(cls) -> (bool, str):
        """
        Generates a new system trust store.
        :return:
        """
        cls.log.info("Generating a new system trust store")

        trustStoreDir = mainCM.getConfigVal("cert.trustStore.systemExternalTrustStoreDir")
        if not os.path.exists(trustStoreDir):
            os.makedirs(trustStoreDir)

        # Copy CA cert to trust store location
        shutil.copyfile(
            mainCM.getConfigVal("cert.selfSigned.certs.CARootCert"),
            mainCM.getConfigVal("cert.trustStore.files.selfSigned.crt")
        )

        #write ca root cert trust store p12
        trust_bundle = []
        with(
            open(mainCM.getConfigVal("cert.selfSigned.certs.CARootCert"), "rb") as root_ca_cert
        ):
            cert = x509.load_pem_x509_certificate(root_ca_cert.read())
            trust_bundle.append(cert)
        with (
            open(mainCM.getConfigVal("cert.trustStore.files.selfSigned.p12"), 'wb') as cert_file
        ):
            cert_file.write(
                pkcs12.serialize_key_and_certificates(
                    name=b"OQM CA",
                    key=None,
                    cert=None,
                    cas=trust_bundle,
                    encryption_algorithm=serialization.BestAvailableEncryption(mainCM.getConfigVal("cert.trustStore.files.selfSigned.p12Password").encode('UTF-8'))
                )
            )

        #ACME / Let's Encrypt
        # TODO: #998 support other ACME providers
        if mainCM.getConfigVal("cert.externalDefault") == "acme":
            trust_bundle = []

            cls.log.info("Adding LetsEncrypt CA to system trust store collection.")

            for certName, certList in cls.LETSE_CA_SOURCES.items():
                for i, curCertUrl in enumerate(certList):
                    certContent = None
                    with(requests.get(curCertUrl) as certRequest):
                        # TODO:: error check
                        certContent = certRequest.content
                    cert = x509.load_pem_x509_certificate(certContent)
                    trust_bundle.append(cert)

            truststoreFile = mainCM.getConfigVal("cert.trustStore.files.acme.crt")
            with (
                open(truststoreFile, 'wb') as cert_file
            ):
                for cert in trust_bundle:
                    cert_file.write(cert.public_bytes(serialization.Encoding.PEM))
        cls.log.info("Finished writing new system trust stores.")


    @classmethod
    def generateRootCA(cls) -> (bool, str):
        """
        Generates a new CA, then installs it on the system.
        :return:
        """
        CertsUtils.log.info("Generating a new root CA")
        root_ca_key_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootPrivateKey")
        root_ca_cert_path = mainCM.getConfigVal("cert.selfSigned.certs.CARootCert")
        ca_private_key = CertsUtils.newPrivateKey()

        ca_name = x509.Name([
            x509.NameAttribute(x509.NameOID.COUNTRY_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.countryName")),
            x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.stateOrProvinceName")),
            x509.NameAttribute(x509.NameOID.LOCALITY_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.localityName")),
            x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.organizationName")),
            x509.NameAttribute(x509.NameOID.COMMON_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.caCommonName")),
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

        cls.generateSystemTruststores()
        returned, caInstallOutput = CertsUtils.ensureCaInstalled()
        if not returned:
            return False, caInstallOutput
        return True, "CA generated."

    @staticmethod
    def generateCert(
            keyFile,
            certFile,
            commonName,
            subjectAltNames=None,
            csrFile=None,
            keystoreFile=None,
            keystorePassword=None
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
            x509.NameAttribute(x509.NameOID.COUNTRY_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.countryName")),
            x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.stateOrProvinceName")),
            x509.NameAttribute(x509.NameOID.LOCALITY_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.localityName")),
            x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, mainCM.getConfigVal("cert.selfSigned.certInfo.organizationName")),
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
            # TODO:: if acme enabled, add let'sEncrypt ca(S) https://letsencrypt.org/certificates/
            # TODO:: contemplate keystore vs trust store. Write out diff ones
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
        return True, "Generated new cert."

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
            mainCM.getConfigVal("cert.additionalExternalSANs"),
            mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCertCsr"),
            mainCM.getConfigVal("cert.selfSigned.certs.systemExternalKeystore") if mainCM.getConfigVal("cert.selfSigned.generateKeystore") else None,
            mainCM.getConfigVal("cert.selfSigned.systemExternalKeystorePass") if mainCM.getConfigVal("cert.selfSigned.generateKeystore") else None
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

    @classmethod
    def ensureSystemCerts(cls, force=False) -> (bool, str, bool):
        """
        Ensures that the external system cert is present, and not expiring soon.
        :param force: If to force writing a new CA anyways
        :return: success, message, if new certs written
        """
        cls.log.info("Ensuring self signed system cert is present")
        systemCertKeyPath = mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCertKey")
        systemCertPath = mainCM.getConfigVal("cert.selfSigned.certs.systemExternalSelfCert")

        needWritten = force
        if not all(os.path.exists(path) for path in [systemCertKeyPath, systemCertPath]):
            cls.log.info("System Cert did NOT exist.")
            needWritten = True
        else:
            rootCert = x509.load_pem_x509_certificate(open(systemCertPath, 'rb').read(), default_backend())
            if cls.certExpiresSoon(rootCert):
                CertsUtils.log.info("System Cert expiring soon.")
                needWritten = True

        if needWritten:
            CertsUtils.log.info("Writing new System Cert.")
            success, message = cls.generateSystemCert()
            cls.generateSystemTruststores()
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
        return (caSuccess and sysSuccess), caMessage + " " + sysMessage, (caWritten or sysWritten)

    @staticmethod
    def generateInternalCert(host, destination) -> (bool, str):
        """
        Wrapper for generateCert(), configured for writing a cert for an internal service to use.
        :return:
        """
        CertsUtils.log.info("Writing new cert for internal service %s", host)
        # shutil.copyfile(mainCM.getConfigVal("cert.trustStore.systemExternalTrustStore"), destination + "/oqmSystemExternalTruststore.p12")
        return CertsUtils.generateCert(
            destination + "/serviceCertKey.pem",
            destination + "/serviceCert.crt",
            host,
            None,
            None,
            destination + "/serviceCertKeystore.p12",
            mainCM.getConfigVal("cert.selfSigned.internalKeystorePass")
        )

    @staticmethod
    def regenCerts() -> (bool, str):
        success, msg = ServiceUtils.doServiceCommand(
            ServiceStateCommand.stop,
            ServiceUtils.SERVICE_ALL
        )

        if not success:
            return False, "FAILED to stop services before cert refresh: " + msg

        success, msg = CertsUtils.ensureCoreCerts(True)

        ServiceUtils.doServiceCommand(
            ServiceStateCommand.start,
            ServiceUtils.SERVICE_ALL
        )
        return success, msg
