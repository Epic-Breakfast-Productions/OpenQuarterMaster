# Cert Notepad

## Helpful commands

 - To check if ssl can verify a cert against a CA: `openssl verify -verbose -CAfile /etc/oqm/certs/oqmCaRootCert.crt /etc/oqm/certs/oqmSystemCert.crt`
 - Examine a cert: `openssl x509 -in /etc/oqm/certs/systemCert.crt --text`