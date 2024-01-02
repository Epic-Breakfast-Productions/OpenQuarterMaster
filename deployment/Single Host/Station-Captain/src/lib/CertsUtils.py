from enum import Enum
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
    def generateSelfSignedCerts(forceRegenRoot:bool=False) -> (bool, str):
        logging.info("Generating self-signed certs")
        try:
            domain = mainCM.getConfigVal("system.hostname")
            root_ca_key_path = mainCM.getConfigVal("cert.certs.CARootPrivateKey")
            root_ca_cert_path = mainCM.getConfigVal("cert.certs.CARootCert")

            # TODO:: ensure directories exist

            #
            # Make RootCA public/private key
            #
            #TODO:: more smartly determine if need to regen based on expiry
            #TODO:: respect method param
            if True or not all(os.path.exists(path) for path in [root_ca_key_path, root_ca_cert_path]):
                private_key = rsa.generate_private_key(
                    public_exponent=65537,
                    key_size=2048,
                    backend=default_backend()
                )

                root_cert = x509.CertificateBuilder().subject_name(
                    x509.Name([
                        x509.NameAttribute(x509.NameOID.COUNTRY_NAME, u'US'),
                        x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u'PA'),
                        x509.NameAttribute(x509.NameOID.LOCALITY_NAME, u''),
                        x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u'OQM-LOCAL-CA'),
                        x509.NameAttribute(x509.NameOID.COMMON_NAME, u'OQM-LOCAL-CA'),
                    ])
                ).issuer_name(
                    x509.Name([
                        x509.NameAttribute(x509.NameOID.COUNTRY_NAME, u'US'),
                        x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u'PA'),
                        x509.NameAttribute(x509.NameOID.LOCALITY_NAME, u''),
                        x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u'OQM-LOCAL-CA'),
                        x509.NameAttribute(x509.NameOID.COMMON_NAME, u'OQM-LOCAL-CA'),
                    ])
                ).public_key(
                    private_key.public_key()
                ).serial_number(
                    x509.random_serial_number()
                ).not_valid_before(
                    datetime.datetime.utcnow()
                ).not_valid_after(
                    datetime.datetime.utcnow() + datetime.timedelta(days=mainCM.getConfigVal("cert.selfMode.rootCaTtl"))
                ).sign(private_key, hashes.SHA256(), default_backend())

                with open(root_ca_key_path, 'wb') as key_file:
                    key_file.write(
                        private_key.private_bytes(
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

            #
            # Make Private key / CSR
            #
            private_key = rsa.generate_private_key(
                public_exponent=65537,
                key_size=2048,
                backend=default_backend()
            )

            csr = x509.CertificateSigningRequestBuilder().subject_name(
                x509.Name([
                    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, 'US'),
                    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u'STAGE'),
                    x509.NameAttribute(x509.NameOID.LOCALITY_NAME, u''),
                    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u'STAGE'),
                    x509.NameAttribute(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, u'STAGE'),
                    x509.NameAttribute(x509.NameOID.COMMON_NAME, domain),
                ])
            ).sign(private_key, hashes.SHA256(), default_backend())

            #
            # Make Cert conf
            #

            # Might not be necessary TODO:: find out
            # cert_conf = "authorityKeyIdentifier=keyid,issuer\n" \
            #             "basicConstraints=CA:FALSE\n" \
            #             "keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment"
            #
            # with open(os.path.join(shared_config_dir, "cert.conf"), 'w') as cert_conf_file:
            #     cert_conf_file.write(cert_conf)

            root_ca_cert = x509.load_pem_x509_certificate(open(root_ca_cert_path, 'rb').read(), default_backend())
            root_ca_key = serialization.load_pem_private_key(open(root_ca_key_path, 'rb').read(), password=None, backend=default_backend())


            cert = x509.CertificateBuilder().subject_name(
                x509.Name([
                    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, u'US'),
                    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u'STAGE'),
                    x509.NameAttribute(x509.NameOID.LOCALITY_NAME, u''),
                    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u'STAGE'),
                    x509.NameAttribute(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, u'STAGE'),
                    x509.NameAttribute(x509.NameOID.COMMON_NAME, domain),
                ])
            ).issuer_name(
                root_ca_cert.subject
            ).public_key(
                csr.public_key()
            ).serial_number(
                x509.random_serial_number()
            ).not_valid_before(
                datetime.datetime.utcnow()
            ).not_valid_after(
                datetime.datetime.utcnow() + datetime.timedelta(days=mainCM.getConfigVal("cert.selfMode.systemCertTtl"))
            ).sign(root_ca_key, hashes.SHA256(), default_backend())

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
            # write out CSR
            with open(mainCM.getConfigVal("cert.selfMode.publicKeyCsr"), 'wb') as csr_file:
                csr_file.write(
                    csr.public_bytes(
                        encoding=serialization.Encoding.PEM
                    )
                )
        except Exception as e:
            logging.error("FAILED to generate new certs: %s", e)
            return False, e
        return True

    @staticmethod
    def getLetsEncryptCerts() -> (bool, str):
        logging.info("Getting Let's Encrypt certs")
        # TODO
        return False, "Not implemented yet."

    @staticmethod
    def regenCerts(forceRegenRoot:bool=False) -> (bool, str):
        logging.info("Re-running cert generation utilities")
        certMode = mainCM.getConfigVal("cert.mode")
        if certMode == "provided":
            return True, "Nothing to do for provided certs."
        if certMode == "self":
            return CertsUtils.generateSelfSignedCerts(forceRegenRoot)
        return False, "Invalid value for config cert.certs.systemCert : " + certMode