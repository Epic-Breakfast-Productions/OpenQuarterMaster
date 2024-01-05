import ipaddress
import shutil
from cryptography.hazmat.primitives._serialization import BestAvailableEncryption
from cryptography.hazmat.primitives.serialization import load_pem_private_key, pkcs12

from ConfigManager import *
import logging
import subprocess
import os
import datetime
from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives import hashes


class CertsUtils:
    """

    """

    @staticmethod
    def generateSelfSignedCerts(forceRegenRoot: bool = False) -> (bool, str):
        logging.info("Generating self-signed certs")
        output = ""
        try:
            domain = mainCM.getConfigVal("system.hostname")
            root_ca_key_path = mainCM.getConfigVal("cert.certs.CARootPrivateKey")
            root_ca_cert_path = mainCM.getConfigVal("cert.certs.CARootCert")

            #
            # Make RootCA public/private key
            #
            # TODO:: more smartly determine if need to regen based on expiry
            if forceRegenRoot or not all(os.path.exists(path) for path in [root_ca_key_path, root_ca_cert_path]):
                output += "Regenerated CA.\n\n"
                ca_private_key = rsa.generate_private_key(
                    public_exponent=65537,
                    key_size=2048,
                    backend=default_backend()
                )

                ca_name = x509.Name([
                    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.countryName")),
                    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.stateOrProvinceName")),
                    x509.NameAttribute(x509.NameOID.LOCALITY_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.localityName")),
                    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.organizationName")),
                    x509.NameAttribute(x509.NameOID.COMMON_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.caCommonName")),
                ])
                nvb = datetime.datetime.utcnow()
                nva = nvb + datetime.timedelta(days=mainCM.getConfigVal("cert.selfMode.rootCaTtl"))

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
                # root_cert.add_extensions([
                #     crypto.X509Extension("subjectKeyIdentifier", False, "hash", subject=ca_cert),
                # ])
                # root_cert.add_extensions([
                #     crypto.X509Extension("authorityKeyIdentifier", False, "keyid:always", issuer=ca_cert),
                # ])
                # root_cert.add_extensions([
                #     x509.Extension
                #     crypto.X509Extension("basicConstraints", False, "CA:TRUE"),
                #     crypto.X509Extension("keyUsage", False, "keyCertSign, cRLSign"),
                # ])
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
                shutil.copy(root_ca_cert_path, "/usr/local/share/ca-certificates/")
                result = subprocess.run(["update-ca-certificates"], shell=False, capture_output=True, text=True, check=True)
                output += "Output from updating system ca certs:\n" + result.stdout +"\n\n"

            #
            # Make Private key / CSR
            #
            private_key = rsa.generate_private_key(
                public_exponent=65537,
                key_size=2048,
                backend=default_backend()
            )

            name = x509.Name([
                x509.NameAttribute(x509.NameOID.COUNTRY_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.countryName")),
                x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.stateOrProvinceName")),
                x509.NameAttribute(x509.NameOID.LOCALITY_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.localityName")),
                x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.organizationName")),
                x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, mainCM.getConfigVal("cert.selfMode.certInfo.organizationalUnitName")),
                x509.NameAttribute(x509.NameOID.COMMON_NAME, domain),
            ])

            csr = (x509.CertificateSigningRequestBuilder()
                   .subject_name(name)
                   .sign(private_key, hashes.SHA256(), default_backend())
                   )

            root_ca_cert = x509.load_pem_x509_certificate(open(root_ca_cert_path, 'rb').read(), default_backend())
            root_ca_key = serialization.load_pem_private_key(open(root_ca_key_path, 'rb').read(), password=None, backend=default_backend())

            nvb = datetime.datetime.utcnow()
            nva = nvb + datetime.timedelta(days=mainCM.getConfigVal("cert.selfMode.systemCertTtl"))

            cert = (x509.CertificateBuilder()
                    .subject_name(name)
                    .issuer_name(root_ca_cert.subject)
                    .serial_number(x509.random_serial_number())
                    .not_valid_before(nvb)
                    .not_valid_after(nva)
                    .add_extension(x509.SubjectAlternativeName([x509.DNSName(domain)]), critical=False)
                    # TODO:: support multiple domains/ip's
                    # .add_extension(x509.IPAddress(ipaddress.IPv4Address(domain)), critical=True)

    .public_key(csr.public_key())
                    .sign(root_ca_key, hashes.SHA256(), default_backend())
                    )

            # Write out private key
            with open(mainCM.getConfigVal("cert.certs.privateKey"), 'wb') as key_file:
                key_file.write(
                    private_key.private_bytes(
                        encoding=serialization.Encoding.PEM,
                        format=serialization.PrivateFormat.TraditionalOpenSSL,
                        encryption_algorithm=serialization.NoEncryption()
                    )
                )
            # Write out system cert
            with open(mainCM.getConfigVal("cert.certs.systemCert"), 'wb') as cert_file:
                cert_file.write(
                    cert.public_bytes(
                        encoding=serialization.Encoding.PEM
                    )
                )
            # Generate Keystore
            with (open(mainCM.getConfigVal("cert.certs.keystore"), 'wb') as cert_file,
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
                        BestAvailableEncryption(bytes(mainCM.getConfigVal("cert.certs.keystorePass"), 'UTF-8'))
                    )
                )
            # write out CSR
            with open(mainCM.getConfigVal("cert.selfMode.publicKeyCsr"), 'wb') as csr_file:
                csr_file.write(
                    csr.public_bytes(
                        encoding=serialization.Encoding.PEM
                    )
                )
            output += "Wrote new private key and cert."
        except Exception as e:
            logging.exception("FAILED to generate new certs: %s", e)
            return False, f"{e}"
        return True, output

    @staticmethod
    def getLetsEncryptCerts() -> (bool, str):
        logging.info("Getting Let's Encrypt certs")
        # TODO
        return False, "Not implemented yet."

    @staticmethod
    def regenCerts(forceRegenRoot: bool = False) -> (bool, str):
        logging.info("Re-running cert generation utilities")
        certMode = mainCM.getConfigVal("cert.mode")
        if certMode == "provided":
            return True, "Nothing to do for provided certs."
        if certMode == "self":
            return CertsUtils.generateSelfSignedCerts(forceRegenRoot)
        return False, "Invalid value for config cert.certs.systemCert : " + certMode

    @staticmethod
    def ensureCertsPresent() -> (bool, str):
        logging.info("Ensuring certs are present.")
        certMode = mainCM.getConfigVal("cert.mode")
        privateKeyLoc = mainCM.getConfigVal("cert.certs.privateKey")
        publicKeyLoc = mainCM.getConfigVal("cert.certs.systemCert")

        missingList = []
        if not os.path.isfile(privateKeyLoc) or not os.path.exists(privateKeyLoc):
            missingList.append("Private Key")
        if not os.path.isfile(publicKeyLoc) or not os.path.exists(publicKeyLoc):
            missingList.append("Public Key/System Cert")
        if not os.path.isfile(publicKeyLoc) or not os.path.exists(publicKeyLoc):
            missingList.append("Keystore")

        output = ""
        if len(missingList) != 0:
            missingList = ", ".join(missingList)
            message = f"{missingList} not present."
            if certMode == "self" or certMode == "letsEncrypt":
                logging.info(message + " Getting.")
                return CertsUtils.regenCerts()
            elif certMode == "provided":
                logging.error(message)
                return False, message
            else:
                return False, "Invalid value for config cert.certs.systemCert : " + certMode
        return True, ""





