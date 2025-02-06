# Cert Notepad

## Helpful commands

 - To check if ssl can verify a cert against a CA: `openssl verify -verbose -CAfile /etc/oqm/certs/oqmCaRootCert.crt /etc/oqm/certs/oqmSystemCert.crt`
 - Examine a cert: `openssl x509 -in /etc/oqm/certs/systemCert.crt --text`
 - `docker run --rm --network=oqm-internal docker.io/curlimages/curl:latest https://oqm-core-base_station:443/q/health -k`
 - `docker run --rm --network=oqm-internal docker.io/alpine/openssl:latest s_client -connect oqm-core-base_station:443 -servername oqm-core-base_station`