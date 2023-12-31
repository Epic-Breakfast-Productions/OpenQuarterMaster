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
    def generateSelfSignedCerts() -> bool:
        logging.info("Generating self-signed certs")
        shared_config_dir = './newcerts'
        domain = "openquartermaster.local"

        if not os.path.exists(shared_config_dir):
            os.makedirs(shared_config_dir)

        root_ca_key_path = os.path.join(shared_config_dir, "root-CA.key")
        root_ca_cert_path = os.path.join(shared_config_dir, "root-CA.crt")

        if not all(os.path.exists(path) for path in [root_ca_key_path, root_ca_cert_path]):
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
                    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u'OQM-CA'),
                    x509.NameAttribute(x509.NameOID.COMMON_NAME, u'OQM-CA'),
                ])
            ).issuer_name(
                x509.Name([
                    x509.NameAttribute(x509.NameOID.COUNTRY_NAME, u'US'),
                    x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u'PA'),
                    x509.NameAttribute(x509.NameOID.LOCALITY_NAME, u''),
                    x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u'OQM-CA'),
                    x509.NameAttribute(x509.NameOID.COMMON_NAME, u'OQM-CA'),
                ])
            ).public_key(
                private_key.public_key()
            ).serial_number(
                x509.random_serial_number()
            ).not_valid_before(
                datetime.datetime.utcnow()
            ).not_valid_after(
                datetime.datetime.utcnow() + datetime.timedelta(days=2920)
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

        private_key = rsa.generate_private_key(
            public_exponent=65537,
            key_size=2048,
            backend=default_backend()
        )

        csr = x509.CertificateSigningRequestBuilder().subject_name(
            x509.Name([
                x509.NameAttribute(x509.NameOID.COUNTRY_NAME, u'US'),
                x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, u'STAGE'),
                x509.NameAttribute(x509.NameOID.LOCALITY_NAME, u''),
                x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, u'STAGE'),
                x509.NameAttribute(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, u'STAGE'),
                x509.NameAttribute(x509.NameOID.COMMON_NAME, domain),
            ])
        ).sign(private_key, hashes.SHA256(), default_backend())

        with open(os.path.join(shared_config_dir, f"{domain}.csr"), 'wb') as csr_file:
            csr_file.write(
                csr.public_bytes(
                    encoding=serialization.Encoding.PEM
                )
            )

        with open(os.path.join(shared_config_dir, f"{domain}.key"), 'wb') as key_file:
            key_file.write(
                private_key.private_bytes(
                    encoding=serialization.Encoding.PEM,
                    format=serialization.PrivateFormat.TraditionalOpenSSL,
                    encryption_algorithm=serialization.NoEncryption()
                )
            )

        cert_conf = "authorityKeyIdentifier=keyid,issuer\n" \
                    "basicConstraints=CA:FALSE\n" \
                    "keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment"

        with open(os.path.join(shared_config_dir, "cert.conf"), 'w') as cert_conf_file:
            cert_conf_file.write(cert_conf)

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
            datetime.datetime.utcnow() + datetime.timedelta(days=365)
        ).sign(root_ca_key, hashes.SHA256(), default_backend())

        with open(os.path.join(shared_config_dir, f"{domain}.crt"), 'wb') as cert_file:
            cert_file.write(
                cert.public_bytes(
                    encoding=serialization.Encoding.PEM
                )
            )

    @staticmethod
    def getLetsEncryptCerts() -> bool:
        logging.info("Getting Let's Encrypt certs")
        # TODO

    @staticmethod
    def regenCerts() -> bool:
        logging.info("Re-running cert generation utilities")
        # TODO:: depending on config, appropriately renew certs. pass when using provided certs