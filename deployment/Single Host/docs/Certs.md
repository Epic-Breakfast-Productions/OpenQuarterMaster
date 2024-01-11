# HTTPS Certs Management Guide

In order to be secure, we ensure all external communication is performed over HTTPS, or in a similar manner.

To support this, we have robust support for SSL certs that integrate with our services to provide this.

To be flexible to your usecase, we support several modes of how these certs are acquired. These are explained below.

**NOTE:** Currently only self-signed is fully implemented. Let's Encrypt still needs work, and Provided should work, as long as you are providing a CA.

## Config

All config related to certs are placed under `cert.*` in config. Explanation below.

| Config Key                    | Default                                  | Explanation                                                                     |
|-------------------------------|------------------------------------------|---------------------------------------------------------------------------------|
| `cert.mode`                   | `self`                                   | The mode of getting certs. Must be one of `self`, `letsEncrypt`, or `provided`. |
| `cert.certs.privateKey`       | `/etc/oqm/certs/oqmSystemPrivateKey.pem` | The location of the system's private key.                                       |
| `cert.certs.systemCert`       | `/etc/oqm/certs/oqmSystemCert.crt`       | The location of the system's public cert/key.                                   |
| `cert.certs.CARootPrivateKey` | `/etc/oqm/certs/oqmCaRootPrivateKey.pem` | The location of the system's CA private key. Only relevant in self mode.        |
| `cert.certs.CARootCert`       | `/etc/oqm/certs/oqmCaRootCert.pem`       | The location of the system's CA public cert/key. Only relevant in self mode.    |
| `cert.certs.keystore`         | `/etc/oqm/certs/oqmSystemKeystore.p12`   | The location of the system's CA in `PKCS12` format. This format is required.    |
| `cert.certs.keystorePass`     | `<secret>`                               | The password for the keystore. Recommend storing as a secret.                   |
| `cert.selfMode.*`             |                                          | Where configuration for Self cert mode. Explained below.                        |
| `cert.letsEncryptMode.*`      |                                          | Where configuration for Let's Encrypt cert mode. Explained below.               |
| `cert.providedMode.*`         |                                          | Where configuration for Provided cert mode. Explained below.                    |

## Modes

### Self (Default)

This is the default, and foolproof, mode. This mode automatically generates certs for the configured hostname (`system.hostname` in config). It also creates a root Certificate Authority (CA), and installs it on the system so system
components can trust each other.

Use this when it is just you or your organization using the software locally, and don't have a cert or CA of your own.

You can copy the file located by `cert.certs.CARootCert` to import on other hosts and browsers so they can trust this system.

#### Config

| Config Key                                      | Default                                      | Explanation                                                |
|-------------------------------------------------|----------------------------------------------|------------------------------------------------------------|
| `cert.selfMode.rootCaTtl`                       | `2920` (8 years)                             | The time to live value for the root CA in days.            |
| `cert.selfMode.systemCertTtl`                   | `365` (1 year)                               | The time to live value for the system cert in days.        |
| `cert.selfMode.certInfo.countryName`            | `US`                                         | The country name to use when generating certs.             |
| `cert.selfMode.certInfo.stateOrProvinceName`    | `PA`                                         | The state or province name to use when generating certs.   |
| `cert.selfMode.certInfo.localityName`           |                                              | The locality name to use when generating certs.            |
| `cert.selfMode.certInfo.organizationName`       | `OQM-LOCAL`                                  | The organization name to use when generating certs.        |
| `cert.selfMode.certInfo.organizationalUnitName` |                                              | The organizational unit name to use when generating certs. |
| `cert.selfMode.certInfo.caCommonName`           | `#{cert.selfMode.certInfo.organizationName}` | The common name to use when generating the CA.             |

Other Relevant config:

| Config Key        | Explanation                                                                                            | Link to more info |
|-------------------|--------------------------------------------------------------------------------------------------------|-------------------|
| `system.hostname` | Used as the common name for the system cert. Also included as a subject alternative name in that cert. |                   |

### Let's Encrypt

WARNING:: this mode is not complete and unsupported at the moment

This mode allows you to get a Let's Encrypt cert automatically. This is useful as the cert will be recognised without any extra importing of certs.

Use when the system is publicly available under a publicly available hostname. Examples of this would be when running on the cloud or otherwise exposing the system to the wider internet.

**NOTE** You must set the config value `system.hostname` to the public hostname you intend to use to access the system. See below for more details.

#### Config

| Config Key                         | Default | Explanation                                                                                                         |
|------------------------------------|---------|---------------------------------------------------------------------------------------------------------------------|
| `cert.letsEncryptMode.acceptTerms` | `false` | A flag to confirm that you agree to Let's Encrypt's terms and conditions for use. Must be set to `true` to operate. |

Other Relevant config:

| Config Key        | Explanation                                                                             | Link to more info |
|-------------------|-----------------------------------------------------------------------------------------|-------------------|
| `system.hostname` | Used as the domain being requested a cert for when making the request to Let's Encrypt. |                   |

### Provided

This mode allows you to provide your own set of certs to use. Simply place your certs in the locations defined by config. It is up to you to provide valid certs.

Use this when you are a larger organization or security nerd who has your own CA or certs.

Notes:

- If providing CA, must provide keystore as well (`cert.certs.keystore`) with your CA in it

#### Config

| Config Key                     | Default | Explanation                                                                  |
|--------------------------------|---------|------------------------------------------------------------------------------|
| `cert.providedMode.caProvided` | `true`  | A flag to determine if the system expects you to have provided a CA as well. |
