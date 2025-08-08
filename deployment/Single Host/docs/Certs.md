# HTTPS Certs Management Guide

[Back](README.md)

In order to be secure, we ensure all external (and most internal) communication is performed over HTTPS.

To support this, we have robust support for SSL certs that integrate with our services to provide this.

To be flexible to your usecase, we support several modes of how these certs are acquired. These are explained below.

[TOC]

## Config

All config related to certs are placed under `cert.*` in config. Explanation below.

| Config Key                                 | Default                                          | Explanation                                                                                                                |
|--------------------------------------------|--------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `cert.externalDefault`                     | `self`                                           | The default cert to configure in the proxy. Must be one of `self`, `acme`, or `provided`.                                  |
| `cert.selfSigned`                          |                                                  | The configuration block for self signed certs. See below for info.                                                         |
| `cert.acme`                                |                                                  | The configuration block for acme (Lets Encrypt) certs. See below for info.                                                 |
| `cert.provided`                            |                                                  | The configuration block for provided certs. See below for info.                                                            |
| `cert.trustStore.systemExternalTrustStore` | `/etc/oqm/certs/oqmSystemExternalTruststore.p12` | The trust store that can be used to interact with the system. Will contain Let's Encrypt CA's as well, if ACME is enabled. |
| `cert.trustStore.systemExternalTrustStore` | `<secret>`                                       | The password for the system truststore.                                                                                    |

## Certs For External Use

The system is quite flexible to what certs are included in the external proxy interface, as well as which cert is
considered 'default'.

There are three modes for defining and including certs for the system to use for external connections. To be clear:

- this concerns what certs are configured in the proxy, and what cert is considered 'default' (as defined by
  `cert.externalDefault`)
- All certs available are included in the proxy.
- Internally used certs are all self-signed, based on the root CA generated.

> [!TIP]
> You can pull the system self-signed CA (`cert.certs.CARootCert`) to another system in order for other browsers or similar to trust the OQM system.
> If connecting with something like Java, we generate a trust store (`cert.trustStore.systemExternalTrustStore`) for the same purpose.

### Self

The system automatically generates a Certificate Authority and certs for automatically encrypting external and internal
traffic. This is intended to provide an out of the box robust and secure system.

The Certificate authority is automatically installed on the system in order to be trusted locally. This root CA (
`cert.certs.CARootCert`) can be copied to other systems in order to establish trust.

The cert used for external communication (self-signed external system cert) automatically includes `system.hostname` as
a domain/IP covered by the cert.

Use this when it is just you or your organization using the software locally, and don't have a cert or CA of your own.

It is important to note that these certs are generated and used internally even when `cert.externalDefault` is not set
to `self`. See "Internal Certs" below for more information.

#### Config

| Config Key                                        | Default                                           | Explanation                                                                                                                                                                                                                                                 |
|---------------------------------------------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `cert.selfSigned.certs.*`                         |                                                   | The configuration block defining the locations of self-signed certs. *Recommend leaving defaults.*                                                                                                                                                          |
| `cert.selfSigned.certs.CARootPrivateKey`          | `/etc/oqm/certs/oqmSystemCaRootPrivateKey.pem`    | Where the self-signed CA Root private key is stored.                                                                                                                                                                                                        |
| `cert.selfSigned.certs.CARootCert`                | `/etc/oqm/certs/oqmSystemCaRootCert.crt`          | Where the self-signed CA Root cert is stored.                                                                                                                                                                                                               |
| `cert.selfSigned.certs.systemExternalSelfCertKey` | `/etc/oqm/certs/oqmSystemExternalSelfCertKey.pem` | Where the self-signed external system cert key is kept. This is the cert included in the proxy.                                                                                                                                                             |
| `cert.selfSigned.certs.systemExternalSelfCert`    | `/etc/oqm/certs/oqmSystemExternalSelfCert.crt`    | Where the self-signed external system cert is kept. This is the cert included in the proxy.                                                                                                                                                                 |
| `cert.selfSigned.certs.systemExternalSelfCertCsr` | `/etc/oqm/certs/oqmSystemExternalSelfCert.csr`    | Where the self-signed external system cert csr is kept.                                                                                                                                                                                                     |
| `cert.selfSigned.certs.systemExternalKeystore`    | `/etc/oqm/certs/oqmSystemExternalKeystore.p12`    | Where the self-signed external system cert keystore is kept. Not used by the system, optionally generated if `cert.selfSigned.generateKeystore` is `true`. Intended to be copied off-system in the case that a developer needs it to connect to the system. |
| `cert.selfSigned.certs.internalKeystorePass`      | `<secret>`                                        | The password to the generated keystores for internal services.                                                                                                                                                                                              |
| `cert.selfSigned.generateKeystore`                | `false`                                           | If to generate the self-signed keystore.                                                                                                                                                                                                                    |
| `cert.selfSigned.systemExternalKeystorePass`      | `<secret>`                                        | The password to the generated system keystore.                                                                                                                                                                                                              |
| `cert.selfSigned.internalKeystorePass`            | `<secret>`                                        | The password to the generated internal keystores.                                                                                                                                                                                                           |
| `cert.selfSigned.rootCaTtl`                       | `2920` (8 years)                                  | The time to live value for the system CA cert in days.                                                                                                                                                                                                      |
| `cert.selfSigned.systemCertTtl`                   | `365` (1 year)                                    | The time to live value for the system cert in days.                                                                                                                                                                                                         |
| `cert.selfSigned.daysBeforeExpiryToRegen`         | `30` (1 month)                                    | The amount of time in days before a cert/CA expires to regenerate.                                                                                                                                                                                          |
| `cert.selfSigned.certInfo.*`                      | `US`                                              | The config block concerned with the information that goes into certs.                                                                                                                                                                                       |
| `cert.selfSigned.certInfo.countryName`            | `US`                                              | The country name to use when generating certs.                                                                                                                                                                                                              |
| `cert.selfSigned.certInfo.stateOrProvinceName`    | `PA`                                              | The state or province name to use when generating certs.                                                                                                                                                                                                    |
| `cert.selfSigned.certInfo.localityName`           |                                                   | The locality name to use when generating certs.                                                                                                                                                                                                             |
| `cert.selfSigned.certInfo.organizationName`       | `OQM-LOCAL`                                       | The organization name to use when generating certs.                                                                                                                                                                                                         |
| `cert.selfSigned.certInfo.organizationalUnitName` |                                                   | The organizational unit name to use when generating certs.                                                                                                                                                                                                  |
| `cert.selfSigned.certInfo.caCommonName`           | `#{cert.selfMode.certInfo.organizationName}`      | The common name to use when generating the CA.                                                                                                                                                                                                              |
| `cert.selfSigned.certInfo.additionalExternalSANs` | `[]`                                              | Additional Subject Alternative Names (DNS entries, IP addresses) to include in the external system cert.                                                                                                                                                    |

Other Relevant config:

| Config Key              | Explanation                                                                                                     | Link to more info |
|-------------------------|-----------------------------------------------------------------------------------------------------------------|-------------------|
| `system.hostname`       | Used as the common name for the external system cert. Also included as a subject alternative name in that cert. |                   |
| `system.altHostnameIps` | Used to add additional valid SAN's to the system cert.                                                          |                   |

### ACME (Let's Encrypt)

When `cert.externalDefault` is set to `acme`, this configures the proxy to reach out to an ACME provider to get a cert
to use for external communication. These certs are only retrieved if set as the default.

By default, the proxy is configured to use the TLS challenge method.

#### Config

| Config Key                  | Default | Explanation                                                                                                                                                                  |
|-----------------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `cert.acme.contactEmail`    |         | The email to use in the ACME request. Needs to be set to use ACME.                                                                                                           |
| `cert.acme.caServer`        |         | The CA Server/endpoint to use for the ACME request. Only used if set. By default, proxy uses Let's Encrypt.                                                                  |
| `cert.acme.eab.*`           |         | The configuration block used to specify External Account Binding for the ACME request. Only used if set. https://doc.traefik.io/traefik/https/acme/#external-account-binding |
| `cert.acme.eab.kid`         |         | The Key identifier from External CA.                                                                                                                                         |
| `cert.acme.eab.hmacEncoded` |         | HMAC key from External CA, should be in Base64 URL Encoding without padding format.                                                                                          |

Other Relevant config:

| Config Key              | Explanation                                             | Link to more info |
|-------------------------|---------------------------------------------------------|-------------------|
| `system.hostname`       | Used as the domain for the ACME request.                |                   |
| `system.altHostnameIps` | Used to add additional valid SAN's to the ACME request. |                   |

### Provided

When `cert.externalDefault` is set to `provided`, this configures the proxy to use certs that are provided by you. Use
this if your company has a cert structure and want this system to follow it.

> [!TIP]
> If using provided certs as default, remember to set `cert.provided.enabled` set to `true`, as well as reviewing the
> rest of the configuration below to ensure success.

#### Config

| Config Key              | Default | Explanation                                                     |
|-------------------------|---------|-----------------------------------------------------------------|
| `cert.provided.enabled` |         | If to include the provided cert in the certs used by the proxy. |
| `cert.provided.cert`    |         | The location on the system where the provided cert resides.     |
| `cert.provided.key`     |         | The location on the system where the provided cert key resides  |

## Internal Certs

Certs are given to services in order to encrypt traffic behind the proxy. These certs are intended to be based on the
self-signed root CA, generated for each service, and regenerated often.

You typically shouldn't need to be concerned with these certs, but it is helpful to know about them.
