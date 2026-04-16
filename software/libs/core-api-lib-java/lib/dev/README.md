# Dev resources

This folder contains files useful for development and testing.

## Certs

The certs in this directory are for testing purposes only. Commands to regenerate them are listed below.


`openssl req -x509 -sha256 -noenc -days 3650 -newkey rsa:2048 -keyout rootCA.key -out rootCA.crt`

`openssl req -newkey rsa:2048 -noenc -keyout testCert.key -out domain.csr`

`domain.ext`:

```text
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
subjectAltName = @alt_names
[alt_names]
DNS.1 = localhost
```

`openssl x509 -req -CA rootCA.crt -CAkey rootCA.key -in domain.csr -out testCert.crt -days 3650 -CAcreateserial -extfile domain.ext`

`openssl pkcs12 -export -in testCert.crt -inkey testCert.key -out testKeystore.p12 -passout pass:mypassword`